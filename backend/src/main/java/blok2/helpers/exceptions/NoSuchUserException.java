package blok2.helpers.exceptions;

public class NoSuchUserException extends IllegalArgumentException {
    public NoSuchUserException(String msg) {
        super(msg);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
