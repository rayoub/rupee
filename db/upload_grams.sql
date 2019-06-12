
CREATE TABLE upload_grams
(
    upload_id SERIAL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_upload_grams_unique ON upload_grams (upload_id);

