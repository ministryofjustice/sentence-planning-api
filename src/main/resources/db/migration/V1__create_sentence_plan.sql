CREATE TABLE OFFENDER
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  OASYS_OFFENDER_ID                 TEXT          NULL,
  NOMIS_OFFENDER_ID                 TEXT          NULL,
  CONSTRAINT offender_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT offender_oasysid_idempotent UNIQUE (OASYS_OFFENDER_ID),
  CONSTRAINT offender_nomisid_idempotent UNIQUE (NOMIS_OFFENDER_ID)
);

CREATE TABLE SENTENCE_PLAN
(
  ID                                 SERIAL       PRIMARY KEY,
  UUID                               UUID         NOT NULL,
  OASYS_ASSSESSMENT_REF              TEXT         NULL,
  STATUS                             TEXT         NOT NULL,
  OFFENDER_ID                        UUID         NOT NULL,
  CREATED_ON                         TIMESTAMP    NOT NULL,
  CONSTRAINT sentence_plan_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT fk_sentenceplan_offender FOREIGN KEY (OFFENDER_ID) REFERENCES OFFENDER (UUID)
);


