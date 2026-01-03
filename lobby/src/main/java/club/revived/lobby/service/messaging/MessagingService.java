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

    public MessagingService(
            final MessageBroker broker,
            final String serviceId
    ) {
        this.broker = broker;
        this.serviceId = serviceId;

        this.broker.subscribe("service-messages-" + serviceId, Envelope.class, this::handleEnvelope);
    }

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

        final Envelope envelope = new Envelope(
                correlationId,
                serviceId,
                targetServiceId,
                request.getClass().getName(),
                gson.toJson(request)
        );

        broker.publish("service-messages-" + targetServiceId, envelope);

        return future.thenApply(responseType::cast);
    }

    public void sendMessage(
            final String targetServiceId,
            final Message message
    ) {
        final Envelope envelope = new Envelope(
                UUID.randomUUID(),
                serviceId,
                targetServiceId,
                message.getClass().getName(),
                gson.toJson(message)
        );

        broker.publish("service-messages-" + targetServiceId, envelope);
    }

    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
        //noinspection unchecked
        requestHandlers.put(requestType.getName(), (Function<Request, Response>) handler);
    }

    public <T extends Message> void registerMessageHandler(
            final Class<T> messageType,
            final Consumer<T> handler
    ) {
        //noinspection unchecked
        messageHandlers.put(messageType.getName(), (Consumer<Message>) handler);
    }

    private void handleEnvelope(final Envelope envelope) {
        if (envelope.targetId().equals(serviceId) || envelope.targetId().equals("global")) {
            if (pendingRequests.containsKey(envelope.correlationId())) {
                handleResponse(envelope);
            } else {
                handleIncoming(envelope);
            }
        }
    }

    private void handleResponse(final Envelope envelope) {
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

    private void handleIncoming(final Envelope envelope) {
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

    private void handleRequest(final Envelope envelope, final Function<Request, Response> handler) {
        try {
            final Class<?> requestType = Class.forName(envelope.payloadType());
            final Request request = (Request) gson.fromJson(envelope.payloadJson(), requestType);
            final Response response = handler.apply(request);

            if (response == null) {
                return;
            }

            final Envelope responseEnvelope = new Envelope(
                    envelope.correlationId(),
                    serviceId,
                    envelope.senderId(),
                    response.getClass().getName(),
                    gson.toJson(response)
            );
            
            broker.publish("service-messages-" + envelope.senderId(), responseEnvelope);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(final Envelope envelope, final Consumer<Message> handler) {
        try {
            final Class<?> messageType = Class.forName(envelope.payloadType());
            final Message message = (Message) gson.fromJson(envelope.payloadJson(), messageType);
            handler.accept(message);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
