
CREATE TABLE alignment_scores
(
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    ce_rmsd NUMERIC NULL DEFAULT 0,
    ce_tm_score NUMERIC NULL DEFAULT -1,
    fatcat_rmsd NUMERIC NULL DEFAULT 0,
    fatcat_tm_score NUMERIC NULL DEFAULT -1
);

CREATE UNIQUE INDEX idx_alignment_scores_unique ON alignment_scores (db_id_1, db_id_2);
