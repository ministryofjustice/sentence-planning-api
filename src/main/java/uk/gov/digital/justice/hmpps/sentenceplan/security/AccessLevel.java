package uk.gov.digital.justice.hmpps.sentenceplan.security;

import lombok.Getter;

@Getter
public enum AccessLevel {
    READ_SENTENCE_PLAN(1), WRITE_SENTENCE_PLAN(2);
    private int level;

    AccessLevel(int level) {
        this.level = level;
    }

}