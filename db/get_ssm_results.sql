
CREATE OR REPLACE FUNCTION get_ssm_results (p_benchmark VARCHAR, p_version VARCHAR, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    tm_q_rmsd NUMERIC,
    tm_q_tm_score NUMERIC,
    tm_avg_rmsd NUMERIC,
    tm_avg_tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH results AS
    (
        SELECT
            COUNT(*) OVER (PARTITION BY r.db_id_1) AS tot,
            RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS n,
            r.db_id_1,
            r.db_id_2,
            s.tm_q_rmsd,
            s.tm_q_tm_score,
            s.tm_avg_rmsd,
            s.tm_avg_tm_score
        FROM
            ssm_result r
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
            r.tm_q_rmsd,
            r.tm_q_tm_score,
            r.tm_avg_rmsd,
            r.tm_avg_tm_score
        FROM
            results r
            INNER JOIN valid_results v
                ON v.db_id = r.db_id_1 
        WHERE
            r.n <= p_limit
    )
    SELECT
        r.n::INTEGER,
        r.db_id_1,
        r.db_id_2,
        r.tm_q_rmsd,
        r.tm_q_tm_score,
        r.tm_avg_rmsd,
        r.tm_avg_tm_score
    FROM 
        filtered_results r
    ORDER BY
        r.db_id_1,
        r.n;

END;
$$LANGUAGE plpgsql;


