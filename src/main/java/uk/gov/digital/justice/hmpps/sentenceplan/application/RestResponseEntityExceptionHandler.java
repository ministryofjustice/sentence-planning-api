package uk.gov.digital.justice.hmpps.sentenceplan.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ErrorResponse;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationExceptions.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ApplicationExceptions.EntityNotFoundException e) {
        log.error("ApplicationExceptions.EntityNotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder().status(404)
                .developerMessage(e.getMessage())
                .userMessage(e.getMessage()).build(), NOT_FOUND);
    }


}
