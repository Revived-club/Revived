package club.revived.lobby.service.broker;

@FunctionalInterface
public interface MessageHandler<T> {
    void handle(T message);
}
