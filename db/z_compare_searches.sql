
WITH rupee_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        tm_rmsd,
        tm_tm_score
    FROM
        get_rupee_results('scop_d50', 'scop_v2_07', 'tm_score', 50)
),
mtm_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        tm_rmsd,
        tm_tm_score
    FROM
        get_mtm_dom_results('scop_d50','scop_v2_07', 50)
),
rupee_eval AS
(
    SELECT
        db_id_1,
        AVG(tm_rmsd) AS avg_tm_rmsd,
        AVG(tm_tm_score) AS avg_tm_tm_score
    FROM
        rupee_results
    GROUP BY
        db_id_1 
),
mtm_eval AS
(
    SELECT
        db_id_1,
        AVG(tm_rmsd) AS avg_tm_rmsd,
        AVG(tm_tm_score) AS avg_tm_tm_score
    FROM
        mtm_results
    GROUP BY
        db_id_1 
)
SELECT
    r.db_id_1,
    CARDINALITY(g.grams) AS residues,
    r.avg_tm_tm_score AS rupee,
    m.avg_tm_tm_score AS mtm,
    CASE WHEN r.avg_tm_tm_score < m.avg_tm_tm_score - 0.03 THEN 1 END AS examine
FROM
    rupee_eval r
    INNER JOIN mtm_eval m
        ON r.db_id_1 = m.db_id_1
    INNER JOIN scop_grams g
        On g.scop_id = r.db_id_1
ORDER BY
    r.db_id_1;


