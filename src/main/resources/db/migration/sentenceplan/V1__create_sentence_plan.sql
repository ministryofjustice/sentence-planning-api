
DROP TABLE IF EXISTS OFFENDER;

CREATE TABLE IF NOT EXISTS OFFENDER
(
  ID                                     SERIAL       PRIMARY KEY,
  UUID                                   UUID         NOT NULL,
  OASYS_OFFENDER_ID                      BIGINT       NULL,
  NOMIS_OFFENDER_ID                      TEXT         NULL,
  DELIUS_OFFENDER_ID                     TEXT         NULL,
  NOMIS_BOOKING_NUMBER                   TEXT         NULL,
  OASYS_OFFENDER_LAST_IMPORTED_ON        TIMESTAMP    NULL,
  CONSTRAINT offender_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT offender_oasysid_idempotent UNIQUE (OASYS_OFFENDER_ID),
  CONSTRAINT offender_nomisid_idempotent UNIQUE (NOMIS_OFFENDER_ID),
  CONSTRAINT offender_nomisbookingnumber_idempotent UNIQUE (NOMIS_BOOKING_NUMBER)
);

DROP TABLE IF EXISTS SENTENCE_PLAN;

CREATE TABLE IF NOT EXISTS SENTENCE_PLAN
(
  ID                                 SERIAL       PRIMARY KEY,
  UUID                               UUID         NOT NULL,
  STARTED_DATE                       TIMESTAMP    NULL,
  COMPLETED_DATE                     TIMESTAMP    NULL,
  DATA                               JSONB        NULL,
  ASSESSMENT_NEEDS_LAST_IMPORTED_ON  TIMESTAMP    NULL,
  OFFENDER_UUID                      UUID         NOT NULL,
  CREATED_ON                         TIMESTAMP    NOT NULL,
  CREATED_BY                         TEXT         NOT NULL,
  MODIFIED_ON                        TIMESTAMP    NULL,
  MODIFIED_BY                        TEXT         NULL,
  CONSTRAINT sentence_plan_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT fk_sentenceplan_offender FOREIGN KEY (OFFENDER_UUID) REFERENCES OFFENDER (UUID)
);

DROP TABLE IF EXISTS NEED;

CREATE TABLE IF NOT EXISTS NEED
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  SENTENCE_PLAN_UUID                UUID          NULL,
  NEED_UUID                         UUID          NULL,
  HEADER                            TEXT          NULL,
  DESCRIPTION                       TEXT          NULL,
  OVER_THRESHOLD                    BOOLEAN       NULL,
  REOFFENDING_RISK                  BOOLEAN       NULL,
  HARM_RISK                         BOOLEAN       NULL,
  LOW_SCORE_RISK                    BOOLEAN       NULL,
  ACTIVE                            BOOLEAN       NOT NULL,
  CREATED_ON                        TIMESTAMP     NOT NULL,
  CONSTRAINT need_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT fk_need_sentenceplan FOREIGN KEY (SENTENCE_PLAN_UUID) REFERENCES SENTENCE_PLAN (UUID)
);


