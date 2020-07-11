
CREATE TABLE ssap_result
(
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    len_1 INTEGER NOT NULL,
    len_2 INTEGER NOT NULL,
    ssap_score NUMERIC NOT NULL,
    aligned_len INTEGER NOT NULL,
    percent_overlap NUMERIC NOT NULL,
    percent_identity NUMERIC NOT NULL,
    rmsd NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_ssap_result_unique ON ssap_result (db_id_1, db_id_2);
