
CREATE TABLE cath_result
(
    cath_id_1 VARCHAR NOT NULL,
    cath_id_2 VARCHAR NOT NULL,
    ssap_score NUMERIC NOT NULL,
    rmsd NUMERIC NOT NULL,
    cecp_rmsd NUMERIC NOT NULL,
    cecp_tm_score NUMERIC NOT NULL,
    fatcat_rmsd NUMERIC NOT NULL,
    fatcat_tm_score NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_cath_result_unique ON cath_result (cath_id_1, cath_id_2);
