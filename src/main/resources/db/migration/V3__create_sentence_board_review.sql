DROP TABLE IF EXISTS SENTENCE_BOARD_REVIEW;

CREATE TABLE IF NOT EXISTS SENTENCE_BOARD_REVIEW
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  SENTENCE_PLAN_UUID                UUID          NULL,
  COMMENTS                          TEXT          NULL,
  ATTENDEES                         TEXT          NULL,
  DATE_OF_BOARD                     TIMESTAMP     NOT NULL,
  CONSTRAINT need_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT fk_need_sentenceplan FOREIGN KEY (SENTENCE_PLAN_UUID) REFERENCES SENTENCE_PLAN (UUID)
);