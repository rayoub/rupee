
WITH rupee_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        tm_q_rmsd AS rupee_rmsd,
        tm_q_tm_score AS rupee_tm_score
    FROM
        get_rupee_results('scop_d360', 'scop_v2_07', 'tm_score', 50)
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
        get_mtm_dom_results('scop_d360','scop_v2_07', 50)
),
rupee_eval AS
(
    SELECT
        db_id_1,
        AVG(rupee_rmsd) AS avg_rupee_rmsd,
        AVG(rupee_tm_score) AS avg_rupee_tm_score
    FROM
        rupee_results
    GROUP BY
        db_id_1 
),
mtm_eval AS
(
    SELECT
        db_id_1,
        AVG(mtm_rmsd) AS avg_mtm_rmsd,
        AVG(mtm_tm_score) AS avg_mtm_tm_score
    FROM
        mtm_results
    GROUP BY
        db_id_1 
),
include_domains AS
(
    SELECT
        r.db_id_1
    FROM
        rupee_eval r
        INNER JOIN mtm_eval m
            ON r.db_id_1 = m.db_id_1
        INNER JOIN scop_grams g
            On g.scop_id = r.db_id_1
    WHERE
        r.avg_rupee_tm_score > m.avg_mtm_tm_score + 0.01
),
missing AS (
    SELECT
        r.n,
        r.db_id_1,
        r.db_id_2,
        r.rupee_tm_score
    FROM
        rupee_result r
        INNER JOIN include_domains d
            ON d.db_id_1 = r.db_id_1
        LEFT JOIN mtm_dom_result_matched m
            ON r.db_id_1 = m.db_id_1 
            AND r.db_id_2 = m.db_id_2
    WHERE
        r.version = 'scop_v2_07'
        AND r.sort_by = 'tm_score'
        AND m.n IS NULL
        AND r.n <= 100
    ORDER BY
        db_id_1,
        n
),
missing_links AS
(
    SELECT
        m.n,
        CARDINALITY(g1.grams) AS len,
        m.db_id_1,
        d1.cl_cf AS cl_cf_1,
        m.db_id_2,
        d2.cl_cf AS cl_cf_2,
        m.rupee_tm_score
    FROM
        missing m
        INNER JOIN scop_domain d1
            ON d1.scop_id = m.db_id_1
        INNER JOIN scop_grams g1
            ON g1.scop_id = d1.scop_id
        INNER JOIN scop_domain d2
            ON d2.scop_id = m.db_id_2
    ORDER BY
        m.db_id_1,
        m.n
)
SELECT
    m.n,
    m.len,
    m.db_id_1,
    m.cl_cf_1,
    left(n1.description, 40),
    m.db_id_2,
    m.cl_cf_2,
    left(n2.description, 40),
    m.rupee_tm_score
FROM
    missing_links m
    INNER JOIN scop_name n1
        ON n1.scop_name = m.cl_cf_1
    INNER JOIN scop_name n2
        ON n2.scop_name = m.cl_cf_2;



