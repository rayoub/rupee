
CREATE TABLE chain_hashes
(
    chain_id VARCHAR NOT NULL,
    min_hashes INTEGER ARRAY NOT NULL,
    band_hashes INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_chain_hashes_unique ON chain_hashes (chain_id);

