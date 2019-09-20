INSERT INTO offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', 11032, null, null);

INSERT INTO sentence_plan (uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES ('11111111-1111-1111-1111-111111111111', 'STARTED', '{"steps": [{"id": "11111111-1111-1111-1111-111111111111" ,"needs": ["11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"], "owner": "[PRACTITIONER]", "status": "COMPLETED", "strength": "strength", "ownerOther": "", "description": "description", "intervention": null}]}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');

INSERT INTO need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, over_threshold, active, created_on) VALUES ('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Alcohol', false, true, false, true, true, '2019-06-27 09:57:32.366026');
INSERT INTO need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, over_threshold, active, created_on) VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Accommodation', false, true, false, true, true, '2019-06-27 09:57:32.366026');

INSERT INTO motivation_ref_data (uuid, motivation_text, friendly_text, created)
VALUES ('38731914-701d-4b4e-abd3-1e0a6375f0b2', 'Pre-contemplation', 'In the dark', '2019-07-24'),
       ('82c563d5-00de-4e17-af88-c0363c4d91cc', 'Contemplation', 'Thinking about it', '2019-07-24'),
       ('55f90269-1d93-4a9a-921a-7bae73f3bf07', 'Preparation', 'Getting ready', '2019-07-24'),
       ('13395347-f226-4cc3-abcf-9dbc224c0f50', 'Action', 'Doing it', '2019-07-24'),
       ('42de6207-7a3a-4ffb-9b45-c3ee4ea514fa', 'Maintenance', 'Keeping going', '2019-07-24'),
       ('76e7770b-2a66-40e4-88b8-ccd038501906', 'Relapse', 'Off track', '2019-07-24'),
       ('2f1a5751-f6ee-450a-9b20-858500cb037e', 'Permanent exit', 'The new me', '2019-07-24');