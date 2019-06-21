package uk.gov.digital.justice.hmpps.sentenceplan.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationExceptions.EntityNotFoundException.class)
    public ResponseEntity handle(ApplicationExceptions.EntityNotFoundException e) {
        log.error("ApplicationExceptions.EntityNotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }


}
