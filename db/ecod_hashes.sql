
CREATE TABLE ecod_hashes
(
    ecod_id VARCHAR NOT NULL,
    min_hashes INTEGER ARRAY NOT NULL,
    band_hashes INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_ecod_hashes_unique ON ecod_hashes (ecod_id);

