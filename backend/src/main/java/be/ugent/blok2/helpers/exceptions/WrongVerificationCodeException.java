package be.ugent.blok2.helpers.exceptions;

public class WrongVerificationCodeException extends RuntimeException {
    public WrongVerificationCodeException(String msg) {
        super(msg);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
