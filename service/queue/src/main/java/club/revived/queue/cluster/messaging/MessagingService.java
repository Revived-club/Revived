package club.revived.queue.cluster.messaging;

import club.revived.queue.cluster.broker.MessageBroker;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class MessagingService {
    private final MessageBroker broker;
    private final String serviceId;
    private final Gson gson = new Gson();
    private final Map<UUID, CompletableFuture<Response>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Response>> pendingGlobalRequests = new ConcurrentHashMap<>();
    private final Map<String, Function<Request, Response>> requestHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Message>> messageHandlers = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> messageRegistry = new ConcurrentHashMap<>();

    /**
     * Creates a MessagingService bound to a specific service instance and subscribes to its messaging channels.
     *
     * Initializes the service with the provided MessageBroker and service identifier, and subscribes to
     * the service-scoped ("service-messages-{serviceId}") and global ("service-messages-global") channels
     * to receive MessageEnvelope instances.
     *
     * @param broker    the MessageBroker used to publish and subscribe messages
     * @param serviceId the identifier for this service instance used to scope the service-specific channel
     */
    public MessagingService(
            final MessageBroker broker,
            final String serviceId
    ) {
        this.broker = broker;
        this.serviceId = serviceId;

        this.broker.subscribe("service-messages-" + serviceId, MessageEnvelope.class, this::handleEnvelope);
        this.broker.subscribe("service-messages-global", MessageEnvelope.class, this::handleEnvelope);
    }

    /**
     * Registers a class for runtime payload type resolution, keyed by the class's simple name.
     *
     * If another class with the same simple name is already registered, it will be replaced.
     *
     * @param clazz the class to register for message (de)serialization and handler lookup
     */
    public void register(final Class<?> clazz) {
        this.messageRegistry.put(clazz.getSimpleName(), clazz);
    }

    /**
     * Sends a request to a specific service and returns a future for the response.
     *
     * Registers the request and response types, publishes the request to the target service's channel,
     * and correlates the eventual response by a generated correlationId.
     *
     * @param targetServiceId the identifier of the target service to receive the request
     * @param request the request payload to send
     * @param responseType the expected response class for deserialization and casting
     * @return a CompletableFuture that completes with the deserialized response of type `T`; the future
     *         completes exceptionally with a `TimeoutException` if no response is received within 5 seconds
     */
    @NotNull
    public <T extends Response> CompletableFuture<T> sendRequest(
            final String targetServiceId,
            final Request request,
            final Class<T> responseType
    ) {
        register(request.getClass());
        register(responseType);

        final UUID correlationId = UUID.randomUUID();
        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            if (pendingRequests.remove(correlationId) != null) {
                future.completeExceptionally(new TimeoutException("Request timed out"));
            }
        });

        final MessageEnvelope envelope = new MessageEnvelope(
                correlationId,
                serviceId,
                targetServiceId,
                request.getClass().getSimpleName(),
                gson.toJson(request)
        );

        broker.publish("service-messages-" + targetServiceId, envelope);

        return future.thenApply(responseType::cast);
    }

    /**
     * Publishes a typed message to the specified target service.
     *
     * @param targetServiceId the identifier of the destination service
     * @param message the message payload to deliver to the target service
     */
    public void sendMessage(
            final String targetServiceId,
            final Message message
    ) {
        register(message.getClass());

        final MessageEnvelope envelope = new MessageEnvelope(
                UUID.randomUUID(),
                serviceId,
                targetServiceId,
                message.getClass().getSimpleName(),
                gson.toJson(message)
        );

        broker.publish("service-messages-" + targetServiceId, envelope);
    }

    /**
     * Publish a fire-and-forget message to the global channel so all services can receive it.
     *
     * @param message the message payload to broadcast to all services
     */
    public void sendGlobalMessage(final Message message) {
        sendMessage("global", message);
    }

    /**
     * Broadcasts the given request to all services and collects their responses.
     *
     * @param request the request payload to send to all services
     * @param responseType the concrete Response class used to deserialize incoming replies
     * @return a list of responses received from services within the collection window (typically ~50 ms); the list may be empty
     */
    @NotNull
    public <T extends Response> CompletableFuture<List<T>> sendGlobalRequest(
            final Request request,
            final Class<T> responseType
    ) {
        register(request.getClass());
        register(responseType);

        final UUID correlationId = UUID.randomUUID();
        final List<Response> responses = new CopyOnWriteArrayList<>();
        final CompletableFuture<List<T>> future = new CompletableFuture<>();

        this.pendingGlobalRequests.put(correlationId, responses);

        CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute(() -> {
            final List<Response> collected = this.pendingGlobalRequests.remove(correlationId);
            if (collected != null) {
                @SuppressWarnings("unchecked")
                List<T> typedResponses = (List<T>) collected;
                future.complete(typedResponses);
            }
        });

        final MessageEnvelope envelope = new MessageEnvelope(
                correlationId,
                serviceId,
                "global",
                request.getClass().getSimpleName(),
                gson.toJson(request)
        );

        broker.publish("service-messages-global", envelope);
        return future;
    }

    /**
     * Registers a handler to process incoming requests of the given type and produce responses.
     *
     * The provided handler will be invoked with a deserialized instance of requests matching
     * the specified requestType; its returned Response will be sent back to the requester.
     *
     * @param requestType the Request class this handler will handle
     * @param handler     a function that takes a request of type `T` and returns a Response
     */
    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
        register(requestType);
        @SuppressWarnings("unchecked")
        Function<Request, Response> uncheckedHandler = (Function<Request, Response>) handler;
        requestHandlers.put(requestType.getSimpleName(), uncheckedHandler);
    }

    /**
     * Register a handler to be invoked for incoming messages of the given type.
     *
     * @param messageType the message class to register and use for runtime type resolution (simple class name used as the registry key)
     * @param handler     consumer that will be called with deserialized instances of the specified message type
     */
    public <T extends Message> void registerMessageHandler(
            final Class<T> messageType,
            final Consumer<T> handler
    ) {
        register(messageType);
        @SuppressWarnings("unchecked")
        Consumer<Message> uncheckedHandler = (Consumer<Message>) handler;
        messageHandlers.put(messageType.getSimpleName(), uncheckedHandler);
    }

    /**
     * Dispatches an incoming MessageEnvelope to the correct handler or pending request list based on its target and correlationId.
     *
     * <p>If the envelope is not addressed to this service or the global channel it is ignored. If its correlationId
     * matches a pending point-to-point request the envelope is treated as a response. If the envelope targets the global
     * channel it is delivered to incoming handlers. If its correlationId matches a pending global request it is treated
     * as a global response. Otherwise the envelope is delivered to incoming handlers for processing as a request or message.
     *
     * @param envelope the incoming envelope whose targetId and correlationId determine routing
     */
    private void handleEnvelope(final MessageEnvelope envelope) {
        if (!envelope.targetId().equals(serviceId) && !envelope.targetId().equals("global")) {
            return;
        }

        if (pendingRequests.containsKey(envelope.correlationId())) {
            handleResponse(envelope);
            return;
        }

        if (envelope.targetId().equals("global")) {
            handleIncoming(envelope);
            return;
        }

        if (pendingGlobalRequests.containsKey(envelope.correlationId())) {
            handleGlobalResponse(envelope);
            return;
        }

        handleIncoming(envelope);
    }

    /**
     * Process an incoming response envelope and complete the matching pending request future.
     *
     * Looks up the pending CompletableFuture by the envelope's correlationId, resolves the payload type
     * from the message registry, deserializes the payload, and completes the future with the resulting
     * Response. If the payload type is not registered or deserialization/processing fails, the future is
     * completed exceptionally. If the deserialized object is not a Response, the future is completed with null.
     *
     * @param envelope the incoming message envelope containing correlationId, payload type, and JSON payload
     */
    private void handleResponse(final MessageEnvelope envelope) {
        final CompletableFuture<Response> future = pendingRequests.remove(envelope.correlationId());
        if (future != null) {
            try {
                final Class<?> responseType = this.messageRegistry.get(envelope.payloadType());

                if (responseType == null) {
                    future.completeExceptionally(new ClassNotFoundException("No class registered for payload type: " + envelope.payloadType()));
                    return;
                }
                final var t = gson.fromJson(envelope.payloadJson(), responseType);

                // TODO: Fix if I can't complete with null
                if (t instanceof final Response response) {
                    future.complete(response);
                }

                future.complete(null);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    /**
     * Collects and stores a response received for a pending global request identified by the envelope's correlationId.
     *
     * If the envelope's payload type is registered and deserializes to a Response, that Response is appended to the
     * list of responses accumulated for the corresponding global request.
     *
     * @param envelope the incoming message envelope containing a correlationId, payloadType, and payloadJson
     */
    private void handleGlobalResponse(final MessageEnvelope envelope) {
        final List<Response> responses = this.pendingGlobalRequests.get(envelope.correlationId());
        if (responses != null) {
            try {
                final Class<?> responseType = this.messageRegistry.get(envelope.payloadType());

                if (responseType == null) {
                    System.err.println("No class registered for payload type: " + envelope.payloadType());
                    return;
                }

                final var t = gson.fromJson(envelope.payloadJson(), responseType);

                if (t instanceof final Response response) {
                    responses.add(response);
                }
            } catch (final Exception e) {
                System.err.println("Error handling global response: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Dispatches an incoming MessageEnvelope to a registered handler based on its payload type.
     *
     * If a registered request handler exists for the envelope's payload type, that handler is invoked;
     * otherwise, if a registered message handler exists for the payload type, that handler is invoked.
     * If no matching handler is found the envelope is ignored.
     *
     * @param envelope the incoming MessageEnvelope whose payload type is used to select and invoke a handler
     */
    private void handleIncoming(final MessageEnvelope envelope) {
        final Function<Request, Response> requestHandler = requestHandlers.get(envelope.payloadType());
        if (requestHandler != null) {
            handleRequest(envelope, requestHandler);
            return;
        }

        final Consumer<Message> messageHandler = messageHandlers.get(envelope.payloadType());
        if (messageHandler != null) {
            handleMessage(envelope, messageHandler);
        }
    }

    /**
     * Handle an incoming request envelope by deserializing its payload, invoking the provided handler,
     * and publishing a response envelope back to the original sender when a non-null Response is produced.
     *
     * If the handler returns `null`, no response is sent.
     *
     * @param envelope the incoming request envelope containing correlation id, sender id, payload type, and JSON payload
     * @param handler  a function that processes the deserialized Request and returns a Response (or `null` to suppress replying)
     * @throws RuntimeException if deserialization or handler execution fails; the original exception is wrapped
     */
    private void handleRequest(final MessageEnvelope envelope, final Function<Request, Response> handler) {
        try {
            final Class<?> requestType = this.messageRegistry.get(envelope.payloadType());
            final Request request = (Request) gson.fromJson(envelope.payloadJson(), requestType);
            final Response response = handler.apply(request);

            if (response == null) {
                return;
            }

            register(response.getClass());

            final MessageEnvelope responseEnvelope = new MessageEnvelope(
                    envelope.correlationId(),
                    serviceId,
                    envelope.senderId(),
                    response.getClass().getSimpleName(),
                    gson.toJson(response)
            );

            broker.publish("service-messages-" + envelope.senderId(), responseEnvelope);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize the envelope's payload into a Message and deliver it to the given handler.
     *
     * @param envelope the incoming message envelope containing `payloadType()` (registered type name) and `payloadJson()` (JSON payload)
     * @param handler  consumer that will be invoked with the deserialized Message
     * @throws RuntimeException if deserialization or handler execution fails
     */
    private void handleMessage(final MessageEnvelope envelope, final Consumer<Message> handler) {
        try {
            final Class<?> messageType = this.messageRegistry.get(envelope.payloadType());
            final Message message = (Message) gson.fromJson(envelope.payloadJson(), messageType);
            handler.accept(message);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}