package uk.gov.digital.justice.hmpps.sentenceplan.application;

public enum LogEvent {

    SENTENCE_PLAN_RETRIEVED,
    SENTENCE_PLAN_CREATED, OASYS_ASSESSMENT_CLIENT_FAILURE, OASYS_ASSESSMENT_NOT_FOUND;

    public static final String EVENT = "event_id";
}
