
WITH rupee_results AS
(
    SELECT
        n,
        db_id_1,
        db_id_2,
        ce_rmsd,
        ce_tm_score
    FROM
        get_rupee_results('scop_d50', 'scop_v2_07', 50)
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
        get_mtm_dom_results('scop_d50','dom_v08_03_2018', 50)
),
rupee_eval AS
(
    SELECT
        db_id_1,
        AVG(ce_rmsd) AS avg_ce_rmsd,
        AVG(ce_tm_score) AS avg_ce_tm_score
    FROM
        rupee_results
    GROUP BY
        db_id_1 
),
mtm_eval AS
(
    SELECT
        db_id_1,
        AVG(ce_rmsd) AS avg_ce_rmsd,
        AVG(ce_tm_score) AS avg_ce_tm_score
    FROM
        mtm_results
    GROUP BY
        db_id_1 
)
SELECT
    r.db_id_1,
    r.avg_ce_tm_score AS rupee,
    m.avg_ce_tm_score AS mtm,
    CASE WHEN r.avg_ce_tm_score < m.avg_ce_tm_score - 0.03 THEN 1 END AS examine
FROM
    rupee_eval r
    INNER JOIN mtm_eval m
        ON r.db_id_1 = m.db_id_1
ORDER BY
    r.avg_ce_tm_score DESC;


