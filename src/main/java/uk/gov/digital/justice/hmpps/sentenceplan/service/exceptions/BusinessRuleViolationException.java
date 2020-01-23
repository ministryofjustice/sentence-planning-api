package uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions;

public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String msg)  {
        super(msg);
    }
}
