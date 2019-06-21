package uk.gov.digital.justice.hmpps.sentenceplan.application;

public interface ApplicationExceptions {

    class EntityNotFoundException extends RuntimeException {

        public EntityNotFoundException(String msg, Object... args) {
            super(String.format(msg, args));
        }

    }
}