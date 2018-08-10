
CREATE TABLE rupee_result
(
    cath_id_1 VARCHAR NOT NULL,
    cath_id_2 VARCHAR NOT NULL,
    cath VARCHAR NOT NULL,
    solid VARCHAR NOT NULL,
    rupee_score NUMERIC NOT NULL,
    cecp_rmsd NUMERIC NOT NULL,
    cecp_tm_score NUMERIC NOT NULL,
    fatcat_rmsd NUMERIC NOT NULL,
    fatcat_tm_score NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_rupee_result_unique ON rupee_result (cath_id_1, cath_id_2);
