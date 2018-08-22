
CREATE TABLE rupee_eval
(
    eval_id SERIAL,
    benchmark VARCHAR NOT NULL,
    version VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    avg_ce_rmsd NUMERIC NOT NULL,
    avg_ce_tm_score NUMERIC NOT NULL,
    num_rows INTEGER NOT NULL
); 
