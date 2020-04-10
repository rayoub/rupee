
CREATE TABLE vast_result
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    vast_score NUMERIC NOT NULL,
    vast_rmsd NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_vast_result_unique ON vast_result (version, n, db_id_1);
