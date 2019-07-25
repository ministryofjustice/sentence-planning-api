DELETE FROM sentenceplanapitest.motivation_ref_data where true;

INSERT INTO sentenceplanapitest.motivation_ref_data (uuid, motivation_text, friendly_text, created) VALUES ('11111111-1111-1111-1111-111111111111', 'Motivation', 'Friendly', now());