
CREATE TABLE rupee_result
(
    version VARCHAR NOT NULL,
    search_mode VARCHAR NOT NULL,
    search_type VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    rupee_rmsd NUMERIC NOT NULL,
    rupee_tm_score NUMERIC NOT NULL,
    rupee_q_score NUMERIC NULL DEFAULT -1,
    rupee_ssap_score NUMERIC NULL DEFAULT -1
); 

CREATE UNIQUE INDEX idx_rupee_result_unique ON rupee_result (version, search_mode, search_type, n, db_id_1);
