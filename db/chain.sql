
CREATE TABLE chain
(
    chain_sid SERIAL,
    chain_id VARCHAR NOT NULL,
    pdb_id VARCHAR NOT NULL,
    chain_name VARCHAR NOT NULL,
    residue_count INTEGER NOT NULL,
    sort_key VARCHAR NOT NULL DEFAULT ''
);

CREATE UNIQUE INDEX idx_chain_unique ON chain (chain_sid);
