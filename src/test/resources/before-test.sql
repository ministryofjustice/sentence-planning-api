DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', null, null, null);

INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Alcohol', false, true, false, true, '2019-06-27 09:57:32.366026');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, description, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Accommodation', false, true, false, true, '2019-06-27 09:57:32.366026');

INSERT INTO sentenceplanapitest.sentence_plan (uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES ('11111111-1111-1111-1111-111111111111', 'STARTED', '{"steps": [ {"id": "11111111-1111-1111-1111-111111111111", "needs": ["11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"], "owner": "PRACTITIONER", "status": "COMPLETE", "strength": "strength", "ownerOther": null, "description": "description", "intervention": null}], "serviceUserComments": "comments", "practitionerComments": "comments 1"}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');

INSERT INTO sentenceplanapitest.sentence_plan (uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES ('22222222-2222-2222-2222-222222222222', 'STARTED', '{"steps": [], "serviceUserComments": "comments", "practitionerComments": "comments 1"}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');