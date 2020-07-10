
CREATE TABLE dir_hashes
(
    db_id VARCHAR NOT NULL,
    min_hashes INTEGER ARRAY NOT NULL,
    band_hashes INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_dir_hashes_unique ON dir_hashes (db_id);

