
CREATE TABLE scop_hashes
(
    db_id VARCHAR NOT NULL,
    set_id VARCHAR NULL,
    min_hashes INTEGER ARRAY NOT NULL,
    band_hashes INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_scop_hashes_unique ON scop_hashes (db_id);

