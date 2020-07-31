package be.ugent.blok2.helpers.exceptions;

public class NoUserLoggedInWithGivenSessionIdMappingException extends Exception {
    public NoUserLoggedInWithGivenSessionIdMappingException() {
        super();
    }

    public NoUserLoggedInWithGivenSessionIdMappingException(String message) {
        super(message);
    }
}
