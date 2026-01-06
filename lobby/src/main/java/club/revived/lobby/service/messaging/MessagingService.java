package club.revived.lobby.service.messaging;

import club.revived.lobby.service.broker.MessageBroker;
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

    public MessagingService(
            final MessageBroker broker,
            final String serviceId
    ) {
        this.broker = broker;
        this.serviceId = serviceId;

        this.broker.subscribe("service-messages-" + serviceId, MessageEnvelope.class, this::handleEnvelope);
        this.broker.subscribe("service-messages-global", MessageEnvelope.class, this::handleEnvelope);
    }

    public void register(final Class<?> clazz) {
        this.messageRegistry.put(clazz.getSimpleName(), clazz);
    }

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

    public void sendGlobalMessage(final Message message) {
        sendMessage("global", message);
    }

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

    public <T extends Request> void registerHandler(
            final Class<T> requestType,
            final Function<T, Response> handler
    ) {
        register(requestType);
        @SuppressWarnings("unchecked")
        Function<Request, Response> uncheckedHandler = (Function<Request, Response>) handler;
        requestHandlers.put(requestType.getSimpleName(), uncheckedHandler);
    }

    public <T extends Message> void registerMessageHandler(
            final Class<T> messageType,
            final Consumer<T> handler
    ) {
        register(messageType);
        @SuppressWarnings("unchecked")
        Consumer<Message> uncheckedHandler = (Consumer<Message>) handler;
        messageHandlers.put(messageType.getSimpleName(), uncheckedHandler);
    }

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