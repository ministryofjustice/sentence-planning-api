package uk.gov.digital.justice.hmpps.sentenceplan.application;

public class ValidationException extends RuntimeException {
    public ValidationException(String msg, Object... args) {
        super(String.format(msg, args));
    }

}