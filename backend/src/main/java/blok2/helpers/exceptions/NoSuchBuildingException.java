package blok2.helpers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoSuchBuildingException extends RuntimeException {
    public NoSuchBuildingException(String authorityNotFound) {
    }
}
