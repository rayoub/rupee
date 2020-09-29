
CREATE TABLE casp_target
(
    db_id VARCHAR NOT NULL,
    residue_count INTEGER NOT NULL
);

CREATE UNIQUE INDEX idx_casp_target_unique ON casp_target (db_id);
