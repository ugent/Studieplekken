package blok2.helpers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends RuntimeException{

    public NotAuthorizedException(String message) {
        super(message);
    }
}
