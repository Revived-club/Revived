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

    /**
     * Create a MessagingService backed by the given MessageBroker and identified by serviceId.
     *
     * Subscribes the broker to the "service-messages-{serviceId}" topic so incoming MessageEnvelope
     * instances for this service are delivered to the internal envelope handler.
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
                request.getClass().getTypeName(),
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
                message.getClass().getTypeName(),
                gson.toJson(message)
        );

        broker.publish("service-messages-" + targetServiceId, envelope);
    }

    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
        //noinspection unchecked
        requestHandlers.put(requestType.getTypeName(), (Function<Request, Response>) handler);
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
        messageHandlers.put(messageType.getTypeName(), (Consumer<Message>) handler);
    }

    /**
     * Routes a received MessageEnvelope to the response handler or to incoming request/message handlers when it is addressed to this service.
     *
     * @param envelope the incoming envelope; if its targetId equals this service's id or "global", it will be treated as a response when its correlationId matches a pending request, otherwise as an incoming request or message
     */
    private void handleEnvelope(final MessageEnvelope envelope) {
        System.out.println("Received request");

        if (!envelope.targetId().equals(serviceId)) {
            System.out.println("serverId != " + envelope.targetId());
            return;
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
                final Class<?> responseType = Class.forName(envelope.payloadType());
                final Response response = (Response) gson.fromJson(envelope.payloadJson(), responseType);

                future.complete(response);
            } catch (final ClassNotFoundException e) {
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
        System.out.println("handle incoming");

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
                    response.getClass().getTypeName(),
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
            final Class<?> messageType = Class.forName(envelope.payloadType());
            final Message message = (Message) gson.fromJson(envelope.payloadJson(), messageType);
            handler.accept(message);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}