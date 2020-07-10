
CREATE TABLE dir_grams
(
    db_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_dir_grams_unique ON dir_grams (db_id);

