
DROP TABLE IF EXISTS MOTIVATION_REF_DATA;

CREATE TABLE IF NOT EXISTS MOTIVATION_REF_DATA
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  MOTIVATION_TEXT                   TEXT          NOT NULL,
  FRIENDLY_TEXT                     TEXT          NOT NULL,
  CREATED                           DATE          NOT NULL,
  DELETED                           DATE,
  CONSTRAINT motivation_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT motivation_mtext_idempotent UNIQUE (MOTIVATION_TEXT),
  CONSTRAINT motivation_ftext_idempotent UNIQUE (FRIENDLY_TEXT)
);
