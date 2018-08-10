
CREATE TABLE chain_grams
(
    chain_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_chain_grams_unique ON chain_grams (chain_id);

