package blok2.exceptions;

public class InvalidRequestParametersException extends RuntimeException {
    public InvalidRequestParametersException(String msg) {
        super(msg);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
