package club.revived.lobby.service.messaging;

import com.google.gson.Gson;
import club.revived.lobby.service.broker.MessageBroker;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private final Map<String, Function<Request, Response>> requestHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Message>> messageHandlers = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> messageRegistry = new ConcurrentHashMap<>();

    /**
     * Constructs a MessagingService backed by the provided MessageBroker and identified by the given serviceId.
     *
     * Subscribes the broker to "service-messages-{serviceId}" and "service-messages-global" so incoming
     * MessageEnvelope instances for this service are delivered to the internal envelope handler.
     *
     * @param broker    the MessageBroker used to publish and receive service messages
     * @param serviceId unique identifier for this service instance; used as the topic suffix for subscriptions
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
     * Registers a class for message payload deserialization using its simple class name as the registry key.
     *
     * @param clazz the class to register; the registry will map `clazz.getSimpleName()` to this class for later deserialization
     */
    public void register(final Class<?> clazz) {
        this.messageRegistry.put(clazz.getSimpleName(), clazz);
    }

    /**
         * Send a request to a target service and correlate the incoming response to this request.
         *
         * @param targetServiceId the identifier of the destination service
         * @param request the request payload to send
         * @param responseType the expected response class used to cast the received payload
         * @return a CompletableFuture that completes with a response of the requested type when a matching response arrives; completes exceptionally with a TimeoutException if no response is received within 5 seconds
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
     * Sends a fire-and-forget message to another service via the message broker.
     *
     * Publishes a MessageEnvelope containing the message (serialized to JSON) and its type
     * to the recipient's topic ("service-messages-{targetServiceId}").
     *
     * @param targetServiceId the identifier of the destination service
     * @param message the message payload to send
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
     * Sends a fire-and-forget message to all services.
     *
     * @param message the message payload to send
     */
    public void sendGlobalMessage(final Message message) {
        sendMessage("global", message);
    }

    /**
     * Sends a request message to all services and awaits the first correlated response.
     *
     * @param request      the request payload to broadcast
     * @param responseType the expected response class for deserializing and casting the reply
     * @return the CompletableFuture that completes with the first matching response of the requested type, or completes exceptionally (for example, with {@link java.util.concurrent.TimeoutException}) if no response arrives within 5 seconds
     */
    @NotNull
    public <T extends Response> CompletableFuture<T> sendGlobalRequest(
            final Request request,
            final Class<T> responseType
    ) {
        return sendRequest("global", request, responseType);
    }

    /**
     * Registers the given request type for deserialization and associates a handler to process incoming requests of that type.
     *
     * @param requestType the request class to register and handle
     * @param handler     function invoked with a deserialized request of type `requestType`; its return value is sent back as the response (if `null`, no response is sent)
     */
    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
        register(requestType);
        //noinspection unchecked
        requestHandlers.put(requestType.getSimpleName(), (Function<Request, Response>) handler);
    }

    /**
     * Registers a handler to process incoming messages of the specified type.
     *
     * @param messageType the Class object for messages the handler should receive
     * @param handler     a consumer invoked with the deserialized message when one arrives
     */
    public <T extends Message> void registerMessageHandler(
            final Class<T> messageType,
            final Consumer<T> handler
    ) {
        register(messageType);
        //noinspection unchecked
        messageHandlers.put(messageType.getSimpleName(), (Consumer<Message>) handler);
    }

    /**
     * Routes a received MessageEnvelope to the response handler or to incoming request/message handlers when it is addressed to this service.
     *
     * @param envelope the incoming envelope; if its targetId equals this service's id or "global", it will be treated as a response when its correlationId matches a pending request, otherwise as an incoming request or message
     */
    private void handleEnvelope(final MessageEnvelope envelope) {
        if (envelope.targetId().contains("global")) {
            System.out.println(envelope);
        }

        if (envelope.targetId().equals(serviceId) || envelope.targetId().equals("global")) {
            if (pendingRequests.containsKey(envelope.correlationId())) {
                handleResponse(envelope);
            } else {
                handleIncoming(envelope);
            }
        }
    }

    /**
         * Matches an incoming response envelope to a pending request and completes the corresponding future.
         *
         * If a pending request with the envelope's correlationId exists, deserializes the envelope payload
         * into the registered response type and completes that request's CompletableFuture with the result.
         * If no class is registered for the payload type, completes the future exceptionally with
         * ClassNotFoundException. Any deserialization or runtime error completes the future exceptionally
         * with the thrown exception.
         *
         * @param envelope the incoming message envelope containing correlationId, payloadType, and payloadJson
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

                final Response response = (Response) gson.fromJson(envelope.payloadJson(), responseType);

                future.complete(response);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    /**
     * Routes an incoming MessageEnvelope to the appropriate registered handler based on its payload type.
     *
     * If a request handler exists for the envelope's payload type the envelope is processed as a request; otherwise, if a message handler exists it is processed as a message. If neither handler is registered the envelope is ignored.
     *
     * @param envelope the incoming envelope whose payloadType determines dispatch target
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
     * Processes an incoming request envelope, invokes the provided handler, and sends a response envelope
     * back to the original sender if the handler returns a non-null Response.
     *
     * @param envelope the incoming MessageEnvelope containing correlation id, sender id, payload type, and JSON payload
     * @param handler  function that accepts a deserialized Request and returns a Response (or null to send no reply)
     * @throws RuntimeException if request deserialization, handler execution, or response serialization/publishing fails
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
     * Deserializes the envelope's payload into a Message and invokes the given handler with it.
     *
     * @param envelope the incoming message envelope containing payloadType and payloadJson
     * @param handler  consumer to process the deserialized Message
     * @throws RuntimeException if the payload type cannot be loaded, deserialization fails, or the handler throws
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