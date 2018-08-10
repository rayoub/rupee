
CREATE OR REPLACE FUNCTION insert_alignment_scores(p_db_id VARCHAR, p_tab alignment_scores ARRAY)
RETURNS VOID
AS $$
BEGIN

    CREATE TEMPORARY TABLE ids 
    (
        db_id VARCHAR NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO ids
    (
        db_id
    )
    SELECT
        db_id_2
    FROM
        alignment_scores
    WHERE
        db_id_1 = p_db_id;
    
	INSERT INTO alignment_scores
    (
        db_id_1,
        db_id_2,
        similarity,
        ce_rmsd,
        ce_tm_score,
        cecp_rmsd,
        cecp_tm_score,
        fatcat_flexible_rmsd,
        fatcat_flexible_tm_score,
        fatcat_rigid_rmsd,
        fatcat_rigid_tm_score
    )
    SELECT
        db_id_1,
        db_id_2,
        similarity,
        ce_rmsd,
        ce_tm_score,
        cecp_rmsd,
        cecp_tm_score,
        fatcat_flexible_rmsd,
        fatcat_flexible_tm_score,
        fatcat_rigid_rmsd,
        fatcat_rigid_tm_score
    FROM
        UNNEST(p_tab) s
        LEFT JOIN ids
            ON s.db_id_2 = ids.db_id
    WHERE
        ids.db_id IS NULL;

END;
$$ LANGUAGE plpgsql;
