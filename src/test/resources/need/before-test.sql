DELETE FROM sentenceplanapitest.motivation_ref_data WHERE true;
DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', 123456, null, null);
INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('22222222-2222-2222-2222-222222222222', 789123, null, null);

-- Full Plan
INSERT INTO sentenceplanapitest.sentence_plan (uuid, created_on, created_by, started_date, completed_date, data, assessment_needs_last_imported_on, offender_uuid)
VALUES ('11111111-1111-1111-1111-111111111111', '2019-11-14 08:11:53.177108', 'system', null, null,
'{
    "comments": {
        "YOUR_RESPONSIVITY": {
            "comment": "a comment",
            "created": [
                2019,
                11,
                14,
                8,
                12,
                0,
                0
            ],
            "createdBy": "ANONYMOUS",
            "commentType": "YOUR_RESPONSIVITY"
        },
        "LIAISON_ARRANGEMENTS": {
            "comment": "another comment",
            "created": [
                2019,
                11,
                14,
                8,
                12,
                0,
                0
            ],
            "createdBy": "ANONYMOUS",
            "commentType": "LIAISON_ARRANGEMENTS"
        }
    },
    "objectives": {
        "59023444-afda-4603-9284-c803d18ee4bb": {
            "id": "59023444-afda-4603-9284-c803d18ee4bb",
            "needs": [
                "850a2ef7-1330-43c0-b4f5-68d1d829d1f1",
                "51c293ec-b2c4-491c-ade5-34375e1cd495"
            ],
            "status": "OPEN",
            "statusChanges": [],
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
                    "description": "Action 2",
                    "motivationUUID": "13395347-f226-4cc3-abcf-9dbc224c0f50",
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
        },
        "a63a8eac-4daf-4801-b32b-e3d20c249ad4": {
            "id": "a63a8eac-4daf-4801-b32b-e3d20c249ad4",
            "needs": [
                "9acddbd3-af5e-4b41-a710-018064700eb5"
            ],
            "actions": {
                "8c88d038-14a8-47d1-aa33-b80295258dfc": {
                    "id": "8c88d038-14a8-47d1-aa33-b80295258dfc",
                    "owner": [
                        "SERVICE_USER"
                    ],
                    "status": "IN_PROGRESS",
                    "created": [
                        2019,
                        11,
                        14,
                        10,
                        19,
                        0,
                        0
                    ],
                    "updated": null,
                    "priority": 0,
                    "progress": [],
                    "ownerOther": null,
                    "targetDate": [
                        2020,
                        3
                    ],
                    "description": "Action 3",
                    "motivationUUID": "8c88d038-14a8-47d1-aa33-b80295258dfc",
                    "interventionUUID": null
                },
                "a4b5d58f-0fb8-4945-890f-ced5b21bdfc2": {
                    "id": "a4b5d58f-0fb8-4945-890f-ced5b21bdfc2",
                    "owner": [
                        "PRACTITIONER"
                    ],
                    "status": "IN_PROGRESS",
                    "created": [
                        2019,
                        11,
                        14,
                        10,
                        20,
                        0,
                        0
                    ],
                    "updated": null,
                    "priority": 1,
                    "progress": [],
                    "ownerOther": null,
                    "targetDate": [
                        2020,
                        10
                    ],
                    "description": "Action 4",
                    "motivationUUID": "8c88d038-14a8-47d1-aa33-b80295258dfc",
                    "interventionUUID": null
                },
                "d79b02c8-504e-4437-bc12-b9823f2e1570": {
                    "id": "d79b02c8-504e-4437-bc12-b9823f2e1570",
                    "owner": [
                        "PRACTITIONER"
                    ],
                    "status": "IN_PROGRESS",
                    "created": [
                        2019,
                        11,
                        15,
                        11,
                        23,
                        0,
                        0
                    ],
                    "updated": null,
                    "priority": 2,
                    "progress": [],
                    "ownerOther": null,
                    "targetDate": [
                        2020,
                        10
                    ],
                    "description": null,
                    "motivationUUID": "11111111-1111-1111-1111-111111111111",
                    "interventionUUID": "b4d75533-a5ac-48f0-84ba-c6a3fa71f3df"
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
            "priority": 1,
            "description": "Objective 2"
        }
    },
    "childSafeguardingIndicated": true
}'
,'2019-11-14 08:11:53.425990', '11111111-1111-1111-1111-111111111111');

-- Empty Plan
INSERT INTO sentenceplanapitest.sentence_plan (uuid, created_on, created_by, started_date, completed_date, data, assessment_needs_last_imported_on, offender_uuid)
VALUES ('22222222-2222-2222-2222-222222222222', '2019-11-14 08:11:53.177108','system', null, null,
'{}'
, '2019-11-14 08:11:53.425990', '22222222-2222-2222-2222-222222222222');

INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('9acddbd3-af5e-4b41-a710-018064700eb5', '11111111-1111-1111-1111-111111111111', null, 'Alcohol', null, true, true, false, true, '2019-11-14 08:11:53.424940');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('51c293ec-b2c4-491c-ade5-34375e1cd495', '11111111-1111-1111-1111-111111111111', null, 'Accommodation', null, true, true, null, true, '2019-11-14 08:11:53.424979');
