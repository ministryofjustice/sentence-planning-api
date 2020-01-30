package uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions;

public class EntityCreationException extends RuntimeException {

        public EntityCreationException(String msg, Object... args) {
            super(String.format(msg, args));
        }

    }