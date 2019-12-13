package uk.gov.digital.justice.hmpps.sentenceplan.security;

public class AuthorisationException extends Throwable {
    public AuthorisationException(String message) {
        super (message);
    }
}
