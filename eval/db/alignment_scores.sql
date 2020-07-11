
CREATE TABLE alignment_scores
(
    version VARCHAR NOT NULL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    tm_q_tm_score NUMERIC NULL DEFAULT -1,
    tm_avg_tm_score NUMERIC NULL DEFAULT -1,
    tm_rmsd NUMERIC NULL DEFAULT -1,
    tm_q_score NUMERIC NULL DEFAULT -1,
    ce_rmsd NUMERIC NULL DEFAULT -1,
    fatcat_rigid_rmsd NUMERIC NULL DEFAULT -1,
    ssap_score NUMERIC NULL DEFAULT -1
);

CREATE UNIQUE INDEX idx_alignment_scores_unique ON alignment_scores (version, db_id_1, db_id_2);
