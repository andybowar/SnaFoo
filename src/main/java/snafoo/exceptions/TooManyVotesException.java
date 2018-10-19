package snafoo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Too many votes.")
public class TooManyVotesException extends RuntimeException {
    public TooManyVotesException() {
        super();
    }

    public TooManyVotesException(String message) {
        super(message);
    }
}
