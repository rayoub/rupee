
CREATE OR REPLACE FUNCTION get_mtm_results (p_benchmark VARCHAR, p_version VARCHAR, p_limit INTEGER)
RETURNS TABLE (
    n INTEGER, 
    db_id_1 VARCHAR,
    db_id_2 VARCHAR,
    mtm_rmsd NUMERIC,
    mtm_tm_score NUMERIC,
    tm_q_tm_score NUMERIC,
    tm_avg_tm_score NUMERIC,
    tm_rmsd NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH gen AS
    (
        SELECT
            n.n,
            b.db_id AS db_id_1
        FROM
            benchmark b
            CROSS JOIN generate_series(1, p_limit) n
        WHERE
            b.name = 'casp_mtm_d246'
        ORDER BY
            b.db_id
    ),
    results AS
    (
        SELECT
            r.n,
            r.db_id_1,
            r.db_id_2,
            r.mtm_rmsd,
            r.mtm_tm_score,
            s.tm_q_tm_score,
            s.tm_avg_tm_score,
            s.tm_rmsd
        FROM
            mtm_result r
            INNER JOIN benchmark b
                ON b.db_id = r.db_id_1
                AND b.name = p_benchmark
            INNER JOIN alignment_scores s
                ON s.db_id_1 = r.db_id_1
                AND s.db_id_2 = r.db_id_2
                AND s.version = p_version
        WHERE
            r.version = p_version 
            AND r.n <= p_limit
    )
    SELECT
        g.n,
        g.db_id_1,
        r.db_id_2,
        r.mtm_rmsd,
        r.mtm_tm_score,
        COALESCE(r.tm_q_tm_score,0) AS tm_q_tm_score,
        COALESCE(r.tm_avg_tm_score,0) AS tm_avg_tm_score,
        r.tm_rmsd
    FROM 
        gen g
        LEFT JOIN results r
            ON r.db_id_1 = g.db_id_1 AND r.n = g.n
    ORDER BY
        g.db_id_1,
        g.n;

END;
$$LANGUAGE plpgsql;


