DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', 123456, null, null);
INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('22222222-2222-2222-2222-222222222222', 789123, null, null);


-- Plan with single Objective and Action
INSERT INTO sentenceplanapitest.sentence_plan (uuid, created_on, created_by, started_date, completed_date, data, assessment_needs_last_imported_on, offender_uuid)
VALUES ('11111111-1111-1111-1111-111111111111', '2019-11-14 08:11:53.177108', 'system', null, null,
'{
    "objectives": {
        "59023444-afda-4603-9284-c803d18ee4bb": {
            "id": "59023444-afda-4603-9284-c803d18ee4bb",
            "needs": [
                "9acddbd3-af5e-4b41-a710-018064700eb5",
                "51c293ec-b2c4-491c-ade5-34375e1cd495"
            ],
            "actions": {
                "0554387d-a19f-4cca-9443-5eb5e339709d": {
                    "id": "0554387d-a19f-4cca-9443-5eb5e339709d",
                    "owner": [
                        "SERVICE_USER"
                    ],
                    "status": "IN_PROGRESS",
                    "created": [
                        2019,
                        11,
                        14,
                        9,
                        10,
                        0,
                        0
                    ],
                    "updated": [
                        2019,
                        11,
                        14,
                        13,
                        40,
                        0,
                        0
                    ],
                    "priority": 0,
                    "progress": [],
                    "ownerOther": null,
                    "targetDate": [
                        2020,
                        10
                    ],
                    "description": "Action 1",
                    "motivationUUID": "55f90269-1d93-4a9a-921a-7bae73f3bf07",
                    "interventionUUID": null
                }
            },
            "created": [
                2019,
                11,
                14,
                8,
                16,
                0,
                0
            ],
            "priority": 0,
            "description": "Objective 1"
        }
    },
    "childSafeguardingIndicated": true
}'
,'2019-11-14 08:11:53.425990', '11111111-1111-1111-1111-111111111111');

-- Plan with 1 objective no ACtions
INSERT INTO sentenceplanapitest.sentence_plan (uuid, created_on, created_by, started_date, completed_date, data, assessment_needs_last_imported_on, offender_uuid)
VALUES ('22222222-2222-2222-2222-222222222222', '2019-11-14 08:11:53.177108', 'system', null, null,
'{
    "objectives": {
        "59023444-afda-4603-9284-c803d18ee4bb": {
            "id": "59023444-afda-4603-9284-c803d18ee4bb",
            "needs": [
                "9acddbd3-af5e-4b41-a710-018064700eb5",
                "51c293ec-b2c4-491c-ade5-34375e1cd495"
            ],
            "actions": {},
            "created": [
                2019,
                11,
                14,
                8,
                16,
                0,
                0
            ],
            "priority": 0,
            "description": "Objective 1"
        }
    },
    "childSafeguardingIndicated": true
}'
, '2019-11-14 08:11:53.425990', '22222222-2222-2222-2222-222222222222');

INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('9acddbd3-af5e-4b41-a710-018064700eb5', '11111111-1111-1111-1111-111111111111', null, 'Thinking and Behaviour', null, true, true, false, true, '2019-11-14 08:11:53.424940');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('51c293ec-b2c4-491c-ade5-34375e1cd495', '11111111-1111-1111-1111-111111111111', null, 'Attitudes', null, true, true, null, true, '2019-11-14 08:11:53.424979');
