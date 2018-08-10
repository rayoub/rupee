
CREATE TABLE cath_eval
(
    eval_id SERIAL,
    description VARCHAR NOT NULL,
    avg_cecp_rmsd NUMERIC NOT NULL,
    avg_cecp_tm_score NUMERIC NOT NULL,
    avg_fatcat_rmsd NUMERIC NOT NULL,
    avg_fatcat_tm_score NUMERIC NOT NULL,
    num_rows INTEGER NOT NULL
); 
