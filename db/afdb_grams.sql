
CREATE TABLE afdb_grams
(
    afdb_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_afdb_grams_unique ON afdb_grams (afdb_id);
