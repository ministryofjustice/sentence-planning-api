DROP TABLE IF EXISTS SENTENCE_BOARD_REVIEW;

CREATE TABLE IF NOT EXISTS SENTENCE_BOARD_REVIEW
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  SENTENCE_PLAN_UUID                UUID          NOT NULL,
  OASYS_OFFENDER_ID                 BIGINT        NOT NULL,
  COMMENTS                          TEXT          NULL,
  ATTENDEES                         TEXT          NULL,
  DATE_OF_BOARD                     TIMESTAMP     NOT NULL,
  CONSTRAINT need_sbr_idempotent UNIQUE (UUID),
  CONSTRAINT fk_sbr_sentenceplan FOREIGN KEY (SENTENCE_PLAN_UUID) REFERENCES SENTENCE_PLAN (UUID),
  CONSTRAINT fk_sbr_offender FOREIGN KEY (OASYS_OFFENDER_ID) REFERENCES OFFENDER (OASYS_OFFENDER_ID)

);