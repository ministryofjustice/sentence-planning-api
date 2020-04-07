package uk.gov.digital.justice.hmpps.sentenceplan.security;

public class AuthorisationException extends RuntimeException {
    public AuthorisationException(String message) {
        super (message);
    }
}
