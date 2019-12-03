DELETE FROM sentenceplanapitest.intervention_ref_Data where true;

INSERT INTO intervention_ref_data (uuid, short_description, description, active, external_reference) VALUES ('11111111-1111-1111-1111-111111111111', 'Inv 1', 'Intervention 1', true, 'INV1');
INSERT INTO intervention_ref_data (uuid, short_description, description, active, external_reference) VALUES ('22222222-2222-2222-2222-222222222222', 'Inv 2', 'Intervention 2', true, 'INV2');
INSERT INTO intervention_ref_data (uuid, short_description, description, active, external_reference) VALUES ('33333333-3333-3333-3333-333333333333', 'Inv 3', 'Intervention 3', true, 'INV3');
INSERT INTO intervention_ref_data (uuid, short_description, description, active, external_reference) VALUES ('44444444-4444-4444-4444-444444444444', 'Inv 4', 'Intervention 4', false, 'INV4');