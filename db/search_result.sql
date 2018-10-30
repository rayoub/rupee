
CREATE TABLE search_result
(
    search_hash VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id VARCHAR NOT NULL,
    pdb_id VARCHAR NOT NULL,
    sort_key VARCHAR NOT NULL,
    similarity NUMERIC NOT NULL,
    rmsd NUMERIC NOT NULL,
    tm_score NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_search_result_unique ON search_result (search_hash, n);

