
WITH rupee_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        tm_avg_rmsd AS rupee_rmsd,
        tm_avg_tm_score AS rupee_tm_score
    FROM
        get_rupee_results('scop_d360', 'scop_v2_07', 'tm_score', 100)
    WHERE
        db_id_1 = 'd1j6va_'
),
mtm_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        tm_q_rmsd AS mtm_rmsd,
        tm_q_tm_score AS mtm_tm_score
    FROM
        get_mtm_dom_results('scop_d360','scop_v2_07', 100)
    WHERE
        db_id_1 = 'd1j6va_'
),
mtm_first AS
(
    SELECT
        m.db_id_1,
        CARDINALITY(g1.grams) AS length_1,
        d1.cl_cf_sf,
        m.db_id_2,
        CARDINALITY(g2.grams) AS length_2,
        d2.cl_cf_sf,
        m.n,
        m.mtm_tm_score,
        r.n,
        r.rupee_tm_score
    FROM
        mtm_results m
        INNER JOIN scop_grams g1
            ON g1.scop_id = m.db_id_1
        INNER JOIN scop_grams g2
            ON g2.scop_id = m.db_id_2
        INNER JOIN scop_domain d1
            ON d1.scop_id = m.db_id_1
        INNER JOIN scop_domain d2
            ON d2.scop_id = m.db_id_2
        LEFT JOIN rupee_results r
            ON m.db_id_2 = r.db_id_2
    ORDER BY
        m.n
),
rupee_first AS
(
    SELECT
        r.db_id_1,
        CARDINALITY(g1.grams) AS length_1,
        d1.cl_cf_sf,
        r.db_id_2,
        CARDINALITY(g2.grams) AS length_2,
        d2.cl_cf_sf,
        r.n,
        r.rupee_tm_score,
        m.n,
        m.mtm_tm_score
    FROM
        rupee_results r
        INNER JOIN scop_grams g1
            ON g1.scop_id = r.db_id_1
        INNER JOIN scop_grams g2
            ON g2.scop_id = r.db_id_2
        INNER JOIN scop_domain d1
            ON d1.scop_id = r.db_id_1
        INNER JOIN scop_domain d2
            ON d2.scop_id = r.db_id_2
        LEFT JOIN mtm_results m
            ON r.db_id_2 = m.db_id_2
    ORDER BY
        r.n
)
SELECT * FROM rupee_first;


