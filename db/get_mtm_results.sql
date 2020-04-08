
-- sort by parameter
-- THRID PARTY
-- 1. ce_rmsd 
-- 2. fatcat_rigid_rmsd
-- RUPEE
-- 3. tm_q_tm_score
-- 4. tm_avg_tm_score
-- 5. tm_rmsd
-- 6. tm_q_score (vs. SSM only)
-- OTHER
-- 7. ssap_score (vs. CATHEDRAL only)

-- valid sort by parameters (others are invalid due to non-matches)
-- 3, 5

CREATE OR REPLACE FUNCTION get_mtm_results (p_benchmark VARCHAR, p_version VARCHAR, p_sort_by INTEGER, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    tm_q_tm_score NUMERIC,
    tm_rmsd NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH gen AS
    (
        -- mtm returns so few results in most cases we need to show what it looks like
        -- so as not to bias against RUPEE
        -- mtm does do very good within the top 10.
        -- the benchmark used makes sure that all return at least 10
        SELECT
            n.n,
            b.db_id AS db_id_1
        FROM
            benchmark b
            CROSS JOIN generate_series(1, p_limit) n
        WHERE
            b.name = p_benchmark
        ORDER BY
            b.db_id
    ),
    results AS
    (
        SELECT
            CASE 
                WHEN p_sort_by = 3 THEN
                    RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY COALESCE(s.tm_q_tm_score, r.mtm_tm_score) DESC, r.db_id_2) 
                WHEN p_sort_by = 5 THEN
                    RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY COALESCE(s.tm_rmsd, r.mtm_rmsd), r.db_id_2) 
            END AS n,
            r.db_id_1,
            r.db_id_2,
            COALESCE(s.tm_q_tm_score, r.mtm_tm_score) AS tm_q_tm_score,
            COALESCE(s.tm_rmsd, r.mtm_rmsd) AS tm_rmsd
        FROM
            mtm_result r
            INNER JOIN benchmark b
                ON b.db_id = r.db_id_1
                AND b.name = p_benchmark
            LEFT JOIN alignment_scores s
                ON s.db_id_1 = r.db_id_1
                AND s.db_id_2 = r.db_id_2
                AND s.version = p_version
        WHERE
            r.version = p_version 
    )
    SELECT
        g.n,
        r.db_id_1,
        r.db_id_2,
        COALESCE(r.tm_q_tm_score, 0),
        COALESCE(r.tm_rmsd, 10) -- 10 should be the average random rmsd
    FROM 
        gen g
        LEFT JOIN results r
            ON r.db_id_1 = g.db_id_1 AND r.n = g.n
    ORDER BY
        g.db_id_1,
        g.n;

END;
$$LANGUAGE plpgsql;


