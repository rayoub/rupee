
WITH rupee_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        ce_rmsd,
        ce_tm_score
    FROM
        get_rupee_results('scop_d3', 'scop_v2_07', 50)
    WHERE
        db_id_1 = 'd1d8la1'
),
mtm_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        ce_rmsd,
        ce_tm_score
    FROM
        get_mtm_dom_results('scop_d3','scop_v2_07', 50)
    WHERE
        db_id_1 = 'd1d8la1'
),
mtm_first AS
(
    SELECT
        m.n,
        m.db_id_2,
        m.ce_tm_score AS mtm_tm_score,
        r.n,
        r.db_id_2,
        r.ce_tm_score AS rupee_tm_score
    FROM
        mtm_results m
        LEFT JOIN rupee_results r
            ON m.db_id_2 = r.db_id_2
    ORDER BY
        m.n
),
rupee_first AS
(
    SELECT
        r.n,
        r.db_id_2,
        r.ce_tm_score AS rupee_tm_score,
        m.n,
        m.db_id_2,
        m.ce_tm_score AS mtm_tm_score
    FROM
        rupee_results r
        LEFT JOIN mtm_results m
            ON r.db_id_2 = m.db_id_2
    ORDER BY
        r.n
)
SELECT * FROM rupee_first;


