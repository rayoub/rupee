
CREATE TABLE scop_grams
(
    scop_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_scop_grams_unique ON scop_grams (scop_id);

