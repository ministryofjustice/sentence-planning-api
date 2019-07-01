DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.assessment where true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (id, uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES (1, '11111111-1111-1111-1111-111111111111', null, null, null);
INSERT INTO sentenceplanapitest.need (id, uuid, assessment_uuid, description, reoffending_risk, harm_risk, low_score_risk, active) VALUES (1, '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Alcohol', false, true, false, true);
INSERT INTO sentenceplanapitest.need (id, uuid, assessment_uuid, description, reoffending_risk, harm_risk, low_score_risk, active) VALUES (2, '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Accommodation', false, true, false, true);
INSERT INTO sentenceplanapitest.assessment (id, uuid, sentence_plan_uuid, assessment_id) VALUES (1, '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'ASSESSMENT ID');
INSERT INTO sentenceplanapitest.sentence_plan (id, uuid, status, data, event_type, created_on, start_date, end_date, offender_uuid) VALUES (1, '11111111-1111-1111-1111-111111111111', 'ACTIVE', '{"actions": [{"needs": ["11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222"], "owner": "PRACTITIONER", "status": "COMPLETE", "strength": "strength", "ownerOther": "", "description": "description", "interventions": {}}], "serviceUserComments": "comments", "practitionerComments": "comments 1"}', '0', '2019-06-27 09:57:32.366026', '2019-06-27 09:57:32.365964', '2019-06-27 09:57:32.365216', '11111111-1111-1111-1111-111111111111');