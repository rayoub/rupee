
CREATE TABLE ecod_grams
(
    ecod_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_ecod_grams_unique ON ecod_grams (ecod_id);

