
CREATE OR REPLACE FUNCTION get_rupee_results (p_benchmark VARCHAR, p_version VARCHAR, p_search_mode VARCHAR, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    rupee_rmsd NUMERIC,
    rupee_tm_score NUMERIC,
    tm_q_tm_score NUMERIC,
    tm_avg_tm_score NUMERIC,
    tm_rmsd NUMERIC
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
            r.rupee_rmsd,
            r.rupee_tm_score,
            s.tm_q_tm_score,
            s.tm_avg_tm_score,
            s.tm_rmsd
        FROM
            rupee_result r
            INNER JOIN benchmark b
                ON b.db_id = r.db_id_1
                AND b.name = p_benchmark
            INNER JOIN alignment_scores s
                ON s.db_id_1 = r.db_id_1
                AND s.db_id_2 = r.db_id_2
                AND s.version = p_version
        WHERE
            r.version = p_version 
            AND r.search_mode = p_search_mode
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
            r.rupee_rmsd,
            r.rupee_tm_score,
            r.tm_q_tm_score,
            r.tm_avg_tm_score,
            r.tm_rmsd
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
        r.rupee_rmsd,
        r.rupee_tm_score,
        r.tm_q_tm_score,
        r.tm_avg_tm_score,
        r.tm_rmsd
    FROM 
        filtered_results r
    ORDER BY
        r.db_id_1,
        r.n;

END;
$$LANGUAGE plpgsql;


