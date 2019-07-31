
DROP TABLE IF EXISTS OFFENDER;

CREATE TABLE IF NOT EXISTS OFFENDER
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  OASYS_OFFENDER_ID                 TEXT          NULL,
  NOMIS_OFFENDER_ID                 TEXT          NULL,
  DELIUS_OFFENDER_ID                TEXT          NULL,
  CONSTRAINT offender_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT offender_oasysid_idempotent UNIQUE (OASYS_OFFENDER_ID),
  CONSTRAINT offender_nomisid_idempotent UNIQUE (NOMIS_OFFENDER_ID)
);

DROP TABLE IF EXISTS SENTENCE_PLAN;

CREATE TABLE IF NOT EXISTS SENTENCE_PLAN
(
  ID                                 SERIAL       PRIMARY KEY,
  UUID                               UUID         NOT NULL,
  STATUS                             TEXT         NOT NULL,
  DATA                               JSONB        NULL,
  EVENT_TYPE                         TEXT         NOT NULL,
  CREATED_ON                         TIMESTAMP    NOT NULL,
  START_DATE                         TIMESTAMP    NOT NULL,
  END_DATE                           TIMESTAMP    NULL,
  OFFENDER_UUID                      UUID         NOT NULL,
  CONSTRAINT sentence_plan_uuid_idempotent UNIQUE (UUID,START_DATE),
  CONSTRAINT fk_sentenceplan_offender FOREIGN KEY (OFFENDER_UUID) REFERENCES OFFENDER (UUID)
);

DROP TABLE IF EXISTS NEED;

CREATE TABLE IF NOT EXISTS NEED
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  SENTENCE_PLAN_UUID                UUID          NULL,
  NEED_UUID                         UUID          NULL,
  DESCRIPTION                       TEXT          NULL,
  OVER_THRESHOLD                    BOOLEAN       NULL,
  REOFFENDING_RISK                  BOOLEAN       NULL,
  HARM_RISK                         BOOLEAN       NULL,
  LOW_SCORE_RISK                    BOOLEAN       NULL,
  ACTIVE                            BOOLEAN       NOT NULL,
  CREATED_ON                        TIMESTAMP     NOT NULL,
  CONSTRAINT need_uuid_idempotent UNIQUE (UUID)
);

DROP TABLE IF EXISTS INTERVENTION;

CREATE TABLE IF NOT EXISTS INTERVENTION
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  SHORT_DESCRIPTION                 TEXT          NOT NULL,
  DESCRIPTION                       TEXT          NOT NULL,
  ACTIVE                            BOOLEAN       NOT NULL,
  CONSTRAINT intervention_uuid_idempotent UNIQUE (UUID)
);
