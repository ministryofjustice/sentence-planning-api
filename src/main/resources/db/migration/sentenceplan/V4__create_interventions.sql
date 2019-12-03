DROP TABLE IF EXISTS INTERVENTION_REF_DATA;

CREATE TABLE IF NOT EXISTS INTERVENTION_REF_DATA
(
  ID                                SERIAL        PRIMARY KEY,
  UUID                              UUID          NOT NULL,
  EXTERNAL_REFERENCE                TEXT          NOT NULL,
  SHORT_DESCRIPTION                 TEXT          NULL,
  DESCRIPTION                       TEXT          NULL,
  ACTIVE                            BOOLEAN       NOT NULL,
  CONSTRAINT intervention_uuid_idempotent UNIQUE (UUID)
);