
CREATE TABLE rupee_result
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    ce_rmsd NUMERIC NOT NULL,
    ce_tm_score NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_rupee_result_unique ON rupee_result (version, n, db_id_1);
