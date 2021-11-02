
CREATE TABLE afdb_hashes
(
    db_id VARCHAR NOT NULL,
    min_hashes INTEGER ARRAY NOT NULL,
    band_hashes INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_afdb_hashes_unique ON afdb_hashes (db_id);
