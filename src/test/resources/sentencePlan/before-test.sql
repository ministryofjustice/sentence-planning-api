DELETE FROM sentenceplanapitest.motivation WHERE true;
DELETE FROM sentencePlanapitest.motivation_ref_data WHERE true;
DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', 123456, null, null);

INSERT INTO sentenceplanapitest.sentence_plan (uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES ('11111111-1111-1111-1111-111111111111', 'STARTED', '{"steps": [ {"id": "11111111-1111-1111-1111-111111111111", "needs": ["11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"], "owner": "PRACTITIONER", "status": "COMPLETED", "strength": "strength", "ownerOther": null, "description": "description", "intervention": null, "progress": []}], "serviceUserComments": "comments"}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');

INSERT INTO sentenceplanapitest.sentence_plan (uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES ('22222222-2222-2222-2222-222222222222', 'STARTED', '{"steps": [], "serviceUserComments": "comments"}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');

INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Alcohol', false, true, false, true, '2019-06-27 09:57:32.366026');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Accommodation', false, true, false, true, '2019-06-27 09:57:32.366026');

INSERT INTO motivation_ref_data (uuid, motivation_text, friendly_text, created)
VALUES ('38731914-701d-4b4e-abd3-1e0a6375f0b2', 'Pre-contemplation', 'In the dark', '2019-07-24'),
       ('82c563d5-00de-4e17-af88-c0363c4d91cc', 'Contemplation', 'Thinking about it', '2019-07-24');