
CREATE TABLE cathedral_result
(
    version VARCHAR NOT NULL,
    n INTEGER NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    ssap_score NUMERIC NOT NULL,
    cathedral_rmsd NUMERIC NOT NULL
); 

CREATE UNIQUE INDEX idx_cathedral_result_unique ON cathedral_result (version, n, db_id_1);

