
DROP TABLE IF EXISTS MOTIVATION_REF_DATA;

CREATE TABLE IF NOT EXISTS MOTIVATION_REF_DATA
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  MOTIVATION_TEXT                   TEXT          NOT NULL,
  FRIENDLY_TEXT                     TEXT          NOT NULL,
  CREATED                           DATE          NOT NULL,
  DELETED                           DATE,
  CONSTRAINT motivation_ref_uuid_idempotent UNIQUE (UUID),
  CONSTRAINT motivation_ref_mtext_idempotent UNIQUE (MOTIVATION_TEXT),
  CONSTRAINT motivation_ref_ftext_idempotent UNIQUE (FRIENDLY_TEXT)
);

DROP TABLE IF EXISTS MOTIVATION;

CREATE TABLE IF NOT EXISTS MOTIVATION
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  NEED_UUID                         TEXT          NOT NULL,
  START_DATE                        DATE          NOT NULL,
  END_DATE                          DATE,
  CONSTRAINT motivation_uuid_idempotent UNIQUE (UUID),
);

--  Index for latest Motivation for a need
CREATE INDEX motivation_need_uuid_end_date_null ON motivation (NEED_UUID, END_DATE) WHERE END_DATE IS NULL;

