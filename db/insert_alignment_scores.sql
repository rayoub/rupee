
CREATE OR REPLACE FUNCTION insert_alignment_scores(p_version VARCHAR, p_db_id VARCHAR, p_tab alignment_scores ARRAY)
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
        version = p_version
        AND db_id_1 = p_db_id;
    
	INSERT INTO alignment_scores
    (
        version,
        db_id_1,
        db_id_2,
        tm_q_tm_score,
        tm_avg_tm_score,
        tm_rmsd,
        tm_q_score,
        ce_rmsd,
        fatcat_rigid_rmsd
    )
    SELECT
        version,
        db_id_1,
        db_id_2,
        tm_q_tm_score,
        tm_avg_tm_score,
        tm_rmsd,
        tm_q_score,
        ce_rmsd,
        fatcat_rigid_rmsd
    FROM
        UNNEST(p_tab) s
        LEFT JOIN ids
            ON s.db_id_2 = ids.db_id
    WHERE
        ids.db_id IS NULL;

END;
$$ LANGUAGE plpgsql;
