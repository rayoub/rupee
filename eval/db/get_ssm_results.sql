
-- sort by parameter
-- 1. tm_q_tm_score
-- 2. tm_avg_tm_score
-- 3. tm_q_score (vs. SSM only)
-- 4. ssap_score (vs. CATHEDRAL only)

CREATE OR REPLACE FUNCTION get_ssm_results (p_benchmark VARCHAR, p_version VARCHAR, p_search_type VARCHAR, p_sort_by INTEGER,  p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    tm_avg_tm_score NUMERIC,
    tm_q_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH results AS
    (
        SELECT
            COUNT(*) OVER (PARTITION BY r.db_id_1) AS tot,
            CASE 
                WHEN p_sort_by = 2 THEN
                    RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC, r.db_id_2) 
                WHEN p_sort_by = 3 THEN
                    RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_score DESC, r.db_id_2) 
            END AS n,
            r.db_id_1,
            r.db_id_2,
            s.tm_avg_tm_score,
            s.tm_q_score
        FROM
            ssm_result r
            INNER JOIN benchmark b
                ON b.db_id = r.db_id_1
                AND b.name = p_benchmark
            INNER JOIN alignment_scores s
                ON s.db_id_1 = r.db_id_1
                AND s.db_id_2 = r.db_id_2
                AND s.version = p_version
        WHERE
            r.version = p_version 
            AND r.search_type = p_search_type
            
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
            r.tm_avg_tm_score,
            r.tm_q_score
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
        r.tm_avg_tm_score,
        r.tm_q_score
    FROM 
        filtered_results r
    ORDER BY
        r.db_id_1,
        r.n;

END;
$$LANGUAGE plpgsql;


