
CREATE TABLE alignment_scores
(
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    similarity NUMERIC(6,4) NOT NULL,
    ce_rmsd NUMERIC(6,4) NOT NULL,
    ce_tm_score NUMERIC(6,4) NOT NULL,
    cecp_rmsd NUMERIC(6,4) NOT NULL,
    cecp_tm_score NUMERIC(6,4) NOT NULL,
    fatcat_flexible_rmsd NUMERIC(6,4) NOT NULL,
    fatcat_flexible_tm_score NUMERIC(6,4) NOT NULL,
    fatcat_rigid_rmsd NUMERIC(6,4) NOT NULL,
    fatcat_rigid_tm_score NUMERIC(6,4) NOT NULL
);

CREATE UNIQUE INDEX idx_alignment_scores_unique ON alignment_scores (db_id_1, db_id_2);
