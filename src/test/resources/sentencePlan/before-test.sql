DELETE FROM sentenceplanapitest.motivation_ref_data WHERE true;
DELETE FROM sentenceplanapitest.need WHERE true;
DELETE FROM sentenceplanapitest.sentence_plan where true;
DELETE FROM sentenceplanapitest.offender WHERE true;

INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('11111111-1111-1111-1111-111111111111', 123456, null, null);
INSERT INTO sentenceplanapitest.offender (uuid, oasys_offender_id, nomis_offender_id, delius_offender_id) VALUES ('22222222-2222-2222-2222-222222222222', 789123, null, null);

INSERT INTO sentenceplanapitest.motivation_ref_data (uuid, motivation_text, friendly_text, created)
VALUES ('38731914-701d-4b4e-abd3-1e0a6375f0b2', 'Pre-contemplation', 'In the dark', '2019-07-24'),
('82c563d5-00de-4e17-af88-c0363c4d91cc', 'Contemplation', 'Thinking about it', '2019-07-24'),
('55f90269-1d93-4a9a-921a-7bae73f3bf07', 'Preparation', 'Getting ready', '2019-07-24'),
('13395347-f226-4cc3-abcf-9dbc224c0f50', 'Action', 'Doing it', '2019-07-24'),
('42de6207-7a3a-4ffb-9b45-c3ee4ea514fa', 'Maintenance', 'Keeping going', '2019-07-24'),
('76e7770b-2a66-40e4-88b8-ccd038501906', 'Relapse', 'Off track', '2019-07-24'),
('2f1a5751-f6ee-450a-9b20-858500cb037e', 'Permanent exit', 'The new me', '2019-07-24');

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
                "84d77a6b-b38a-4e9b-97c2-7b98f5af9cf7"
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
                    "progress": [
                        {
                            "status": "IN_PROGRESS",
                            "comment": "a comment",
                            "created": [
                                2019,
                                11,
                                14,
                                13,
                                39,
                                0,
                                0
                            ],
                            "createdBy": "ANONYMOUS",
                            "targetDate": [
                                2020,
                                10
                            ],
                            "motivationUUID": "55f90269-1d93-4a9a-921a-7bae73f3bf07"
                        },
                        {
                            "status": "IN_PROGRESS",
                            "comment": "a comment",
                            "created": [
                                2019,
                                11,
                                14,
                                13,
                                39,
                                0,
                                0
                            ],
                            "createdBy": "ANONYMOUS",
                            "targetDate": [
                                2020,
                                10
                            ],
                            "motivationUUID": "13395347-f226-4cc3-abcf-9dbc224c0f50"
                        },
                        {
                            "status": "IN_PROGRESS",
                            "comment": "a comment",
                            "created": [
                                2019,
                                11,
                                14,
                                13,
                                40,
                                0,
                                0
                            ],
                            "createdBy": "ANONYMOUS",
                            "targetDate": [
                                2020,
                                10
                            ],
                            "motivationUUID": "55f90269-1d93-4a9a-921a-7bae73f3bf07"
                        }
                    ],
                    "ownerOther": null,
                    "targetDate": [
                        2020,
                        10
                    ],
                    "description": "Action 1",
                    "motivationUUID": "55f90269-1d93-4a9a-921a-7bae73f3bf07",
                    "interventionUUID": null
                },
                "6b3186da-56dc-4668-b8ed-aec1584ac548": {
                    "id": "6b3186da-56dc-4668-b8ed-aec1584ac548",
                    "owner": [
                        "PRACTITIONER"
                    ],
                    "status": "PARTIALLY_COMPLETED",
                    "created": [
                        2019,
                        11,
                        14,
                        9,
                        12,
                        0,
                        0
                    ],
                    "updated": [
                        2019,
                        11,
                        15,
                        11,
                        20,
                        0,
                        0
                    ],
                    "priority": 1,
                    "progress": [
                        {
                            "status": "PARTIALLY_COMPLETED",
                            "comment": "a comment",
                            "created": [
                                2019,
                                11,
                                15,
                                11,
                                20,
                                0,
                                0
                            ],
                            "createdBy": "ANONYMOUS",
                            "targetDate": [
                                2020,
                                10
                            ],
                            "motivationUUID": "13395347-f226-4cc3-abcf-9dbc224c0f50"
                        }
                    ],
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
                "9acddbd3-af5e-4b41-a710-018064700eb5",
                "0b77def6-3d96-4d24-80c7-43e37600b304"
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

INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('9acddbd3-af5e-4b41-a710-018064700eb5', '11111111-1111-1111-1111-111111111111', null, 'Thinking and Behaviour', null, true, true, false, true, '2019-11-14 08:11:53.424940');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('51c293ec-b2c4-491c-ade5-34375e1cd495', '11111111-1111-1111-1111-111111111111', null, 'Attitudes', null, true, true, null, true, '2019-11-14 08:11:53.424979');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('0b77def6-3d96-4d24-80c7-43e37600b304', '11111111-1111-1111-1111-111111111111', null, 'Accommodation', null, true, true, false, true, '2019-11-14 08:11:53.425003');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('9cbc011c-885f-4956-9903-1afc02e3a5f8', '11111111-1111-1111-1111-111111111111', null, 'Education, Training and Employability', null, true, true, false, true, '2019-11-14 08:11:53.425015');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('850a2ef7-1330-43c0-b4f5-68d1d829d1f1', '11111111-1111-1111-1111-111111111111', null, 'Financial Management and Income', null, true, true, null, true, '2019-11-14 08:11:53.425039');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('75c55d33-3bf7-43e1-ace8-3fbbaf1e337c', '11111111-1111-1111-1111-111111111111', null, 'Relationships', null, true, true, false, true, '2019-11-14 08:11:53.425061');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ('84d77a6b-b38a-4e9b-97c2-7b98f5af9cf7', '11111111-1111-1111-1111-111111111111', null, 'Lifestyle and Associates', null, true, true, false, true, '2019-11-14 08:11:53.425083');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ( '368b518b-2b96-419e-8654-8bb3fc253b7a', '11111111-1111-1111-1111-111111111111', null, 'Alcohol Misuse', null, true, true, false, true, '2019-11-14 08:11:53.425102');
INSERT INTO sentenceplanapitest.need (uuid, sentence_plan_uuid, need_uuid, description, over_threshold, reoffending_risk, harm_risk, low_score_risk, active, created_on) VALUES ( 'e7628fe8-7079-4841-b7ae-765ebc9e4881', '11111111-1111-1111-1111-111111111111', null, 'Emotional Well-Being', null, true, true, null, true, '2019-11-14 08:11:53.425114');

INSERT INTO sentenceplanapitest.sentence_board_review (uuid, sentence_plan_uuid, comments, attendees, date_of_board) VALUES ( '11111111-4444-4444-4444-111111111111', '11111111-1111-1111-1111-111111111111', 'Any Comments', 'Any Attendees', '2019-11-14 08:11:53.425114');
