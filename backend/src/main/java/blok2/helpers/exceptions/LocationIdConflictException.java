package blok2.helpers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class LocationIdConflictException extends RuntimeException {
    public LocationIdConflictException(String msg) {
        super(msg);
    }
}
