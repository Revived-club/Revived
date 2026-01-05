package club.revived.duels.service.messaging;

import club.revived.duels.service.broker.MessageBroker;
import com.google.gson.Gson;
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
     * Construct a MessagingService that uses the given MessageBroker and service identifier.
     *
     * Subscribes the provided broker to the topic "service-messages-{serviceId}" so incoming
     * MessageEnvelope instances addressed to this service are delivered to the service's envelope handler.
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
    }

    public void register(final Class<?> clazz) {
        this.messageRegistry.put(clazz.getSimpleName(), clazz);
    }


    /**
     * Send a request message to another service and await a correlated response.
     *
     * @param targetServiceId the identifier of the destination service
     * @param request the request payload to send
     * @param responseType the expected response class used to cast the received payload
     * @return a CompletableFuture that completes with a response of the requested type when a matching response arrives; completes exceptionally (for example, with TimeoutException) if no response is received within 5 seconds
     */
    @NotNull
    public <T extends Response> CompletableFuture<T> sendRequest(
            final String targetServiceId,
            final Request request,
            final Class<T> responseType
    ) {
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
     * Registers a handler to process incoming requests of the specified type.
     *
     * If the handler returns a non-null Response, that response will be sent back to the original requester.
     * If the handler returns null, no response will be sent.
     *
     * @param requestType the concrete Request class to handle
     * @param handler     a function that accepts a deserialized request instance and produces a Response (or null)
     */
    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
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
        //noinspection unchecked
        messageHandlers.put(messageType.getSimpleName(), (Consumer<Message>) handler);
    }

    /**
     * Route a received MessageEnvelope to the appropriate internal handler when it is addressed to this service.
     *
     * If the envelope's targetId equals this service's id or "global", the envelope is delivered to a matching pending-request response handler when its correlationId matches an in-flight request; otherwise it is dispatched to registered request or message handlers. Envelopes not addressed to this service are ignored.
     *
     * @param envelope the incoming envelope to route; processed only when its targetId is this service's id or "global"
     */
    private void handleEnvelope(final MessageEnvelope envelope) {
        if (envelope.targetId().equals(serviceId) || envelope.targetId().equals("global")) {
            if (pendingRequests.containsKey(envelope.correlationId())) {
                handleResponse(envelope);
            } else {
                handleIncoming(envelope);
            }
        }
    }

    /**
     * Completes a pending request future (if any) using the envelope's payload as the response.
     *
     * If a pending request with the envelope's correlationId exists, deserializes the payloadJson
     * into the payloadType and completes the corresponding CompletableFuture with the resulting
     * Response; if the payload type class cannot be found, completes the future exceptionally
     * with a ClassNotFoundException.
     *
     * @param envelope the incoming message envelope containing correlationId, payloadType and payloadJson
     */
    private void handleResponse(final MessageEnvelope envelope) {
        final CompletableFuture<Response> future = pendingRequests.remove(envelope.correlationId());
        if (future != null) {
            try {
                final Class<?> responseType = this.messageRegistry.get(envelope.payloadType());
                final Response response = (Response) gson.fromJson(envelope.payloadJson(), responseType);

                future.complete(response);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        }
    }

    /**
     * Dispatches an incoming MessageEnvelope to a registered request or message handler based on the envelope's payload type.
     *
     * If a request handler is registered for the payload type, the envelope is handled as a request; otherwise, if a message handler is registered, it is handled as a message. If no handler is registered for the payload type, the envelope is ignored.
     *
     * @param envelope the incoming MessageEnvelope whose payloadType determines which handler should process it
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
            final Class<?> requestType = Class.forName(envelope.payloadType());
            final Request request = (Request) gson.fromJson(envelope.payloadJson(), requestType);
            final Response response = handler.apply(request);

            if (response == null) {
                return;
            }

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
         * Deserialize the envelope's payload into a Message and invoke the given handler with it.
         *
         * @param envelope the incoming envelope containing the payload type name and JSON payload
         * @param handler  consumer that will be invoked with the deserialized Message
         * @throws RuntimeException if the payload class cannot be loaded, deserialization fails, or the handler throws
         */
    private void handleMessage(final MessageEnvelope envelope, final Consumer<Message> handler) {
        try {
            final Class<?> messageType = Class.forName(envelope.payloadType());
            final Message message = (Message) gson.fromJson(envelope.payloadJson(), messageType);
            handler.accept(message);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}