package uk.gov.digital.justice.hmpps.sentenceplan.application;

public enum LogEvent {

    SENTENCE_PLAN_RETRIEVED,
    SENTENCE_PLAN_CREATED,
    OASYS_ASSESSMENT_CLIENT_FAILURE,
    OASYS_ASSESSMENT_NOT_FOUND,
    SENTENCE_PLAN_NEEDS_RETRIEVED,
    SENTENCE_PLAN_COMMENTS_CREATED,
    SENTENCE_PLAN_COMMENTS_RETRIEVED,
    SENTENCE_PLAN_ACTION_RETRIEVED,
    SENTENCE_PLAN_ACTION_UPDATED,
    SENTENCE_PLAN_ACTION_CREATED,
    SENTENCE_PLAN_OBJECTIVE_RETRIEVED,
    SENTENCE_PLAN_OBJECTIVE_UPDATED,
    SENTENCE_PLAN_OBJECTIVE_CREATED,
    SENTENCE_PLAN_ACTION_PRIORITY_UPDATED,
    SENTENCE_PLANS_RETRIEVED;

    public static final String EVENT = "event_id";
}
