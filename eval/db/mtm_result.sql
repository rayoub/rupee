
CREATE TABLE mtm_result
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    mtm_rmsd NUMERIC NOT NULL,
    mtm_tm_score NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_mtm_result_unique ON mtm_result (version, n, db_id_1);
