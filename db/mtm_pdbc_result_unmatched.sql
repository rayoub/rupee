
CREATE TABLE mtm_pdbc_result_unmatched
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    mtm_rmsd NUMERIC NOT NULL,
    mtm_tm_score NUMERIC NOT NULL,
    ce_rmsd NUMERIC NULL DEFAULT 0,
    ce_tm_score NUMERIC NULL DEFAULT -1
); 

CREATE UNIQUE INDEX idx_mtm_pdbc_result_unmatched_unique ON mtm_pdbc_result_unmatched (version, n, db_id_1);
