
CREATE OR REPLACE FUNCTION get_mtm_results (p_benchmark VARCHAR, p_version VARCHAR, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    tm_q_tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH gen AS
    (
        -- mTM very often doesn't return enough results
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
            RANK(*) OVER (PARTITION BY r.db_id_1 ORDER BY COALESCE(s.tm_q_tm_score, r.mtm_tm_score) DESC, r.db_id_2) AS n,
            r.db_id_1,
            r.db_id_2,
            COALESCE(s.tm_q_tm_score, r.mtm_tm_score) AS tm_q_tm_score
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
        COALESCE(r.tm_q_tm_score, 0)
    FROM 
        gen g
        LEFT JOIN results r
            ON r.db_id_1 = g.db_id_1 AND r.n = g.n
    ORDER BY
        g.db_id_1,
        g.n;

END;
$$LANGUAGE plpgsql;


