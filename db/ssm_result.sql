
CREATE TABLE ssm_result
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    ssm_rmsd NUMERIC NOT NULL,
    ssm_q_score NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_ssm_result_unique ON ssm_result (version, n, db_id_1);
