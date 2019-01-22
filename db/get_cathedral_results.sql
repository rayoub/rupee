
CREATE OR REPLACE FUNCTION get_cathedral_results (p_benchmark VARCHAR, p_version VARCHAR, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    ce_rmsd NUMERIC,
    ce_tm_score NUMERIC,
    fatcat_rmsd NUMERIC,
    fatcat_tm_score NUMERIC,
    tm_rmsd NUMERIC,
    tm_tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH results AS
    (
        SELECT
            COUNT(*) OVER (PARTITION BY r.db_id_1) AS tot,
            r.n,
            r.db_id_1,
            r.db_id_2,
            s.ce_rmsd,
            s.ce_tm_score,
            s.fatcat_rmsd,
            s.fatcat_tm_score,
            s.tm_rmsd,
            s.tm_tm_score
        FROM
            cathedral_result r
            INNER JOIN benchmark b
                ON b.db_id = r.db_id_1
                AND b.name = p_benchmark
            INNER JOIN alignment_scores s
                ON s.db_id_1 = r.db_id_1
                AND s.db_id_2 = r.db_id_2
                AND s.version = p_version
    ),
    valid_results As
    (
        -- at least limit number of results
        SELECT
            r.db_id_1 AS db_id
        FROM
            results r
        WHERE
            r.n = 1 AND r.tot >= p_limit
    ),
    filtered_results AS
    (
        SELECT
            r.n,
            r.db_id_1,
            r.db_id_2,
            r.ce_rmsd,
            r.ce_tm_score,
            r.fatcat_rmsd,
            r.fatcat_tm_score,
            r.tm_rmsd,
            r.tm_tm_score
        FROM
            results r
            INNER JOIN valid_results v
                ON v.db_id = r.db_id_1 
        WHERE
            r.n <= p_limit
    )
    SELECT
        r.n,
        r.db_id_1,
        r.db_id_2,
        r.ce_rmsd,
        r.ce_tm_score,
        r.fatcat_rmsd,
        r.fatcat_tm_score,
        r.tm_rmsd,
        r.tm_tm_score
    FROM 
        filtered_results r
    ORDER BY
        r.db_id_1,
        r.n;

END;
$$LANGUAGE plpgsql;


