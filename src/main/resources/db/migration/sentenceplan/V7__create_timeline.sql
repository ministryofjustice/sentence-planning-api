DROP TABLE IF EXISTS timeline cascade;

CREATE TABLE IF NOT EXISTS timeline
(
    ID                     BIGSERIAL   PRIMARY KEY,
    UUID                   UUID        NOT NULL,
    SENTENCE_PLAN_UUID     UUID,
    PAYLOAD                JSONB,
    EVENT_TIMESTAMP        TIMESTAMP   NOT NULL,
    TYPE                   TEXT        NOT NULL,
    USER_ID                TEXT        NOT NULL,


    CONSTRAINT timeline_data_uuid_idempotent UNIQUE (UUID)
);

CREATE INDEX idx_sentence_plan_uuid ON timeline (SENTENCE_PLAN_UUID);