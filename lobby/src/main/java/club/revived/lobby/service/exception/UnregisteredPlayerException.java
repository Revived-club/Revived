package club.revived.lobby.service.exception;

public class UnregisteredPlayerException extends RuntimeException {
    public UnregisteredPlayerException(String message) {
        super(message);
    }
}
