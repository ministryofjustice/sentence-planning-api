CREATE SEQUENCE hibernate_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

DROP TABLE IF EXISTS revinfo;

CREATE TABLE revinfo
(
    rev integer NOT NULL,
    revtstmp bigint,
    CONSTRAINT revinfo_pkey PRIMARY KEY (rev)
);

DROP TABLE IF EXISTS sentence_plan_aud;

CREATE TABLE sentence_plan_aud
(
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    assessment_needs_last_imported_on timestamp,
    created_by character varying(255),
    created_on timestamp without time zone,
    data jsonb,
    end_date timestamp without time zone,
    event_type integer,
    modified_on timestamp without time zone,
    modified_by character varying(255) COLLATE pg_catalog."default",
    started_date timestamp without time zone,
    completed_date timestamp without time zone,
    status character varying(255) COLLATE pg_catalog."default",
    uuid uuid,
    offender_uuid bigint,
    CONSTRAINT sentence_plan_aud_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fkrkkawcrbdlvj7316wrytv3i22 FOREIGN KEY (rev)
        REFERENCES revinfo (rev) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);