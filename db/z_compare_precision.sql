



WITH all_rupee_tm_score AS
(
    SELECT * FROM get_rupee_results('scop_d360', 'scop_v2_07', 'tm_score', 100)
),
all_other AS
(   
    SELECT * FROM get_mtm_dom_results('scop_d360', 'scop_v2_07', 100)
),
valid_rupee_id AS
(
    SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_tm_score
),
valid_other_id AS
(
    SELECT DISTINCT db_id_1 AS db_id FROM all_other
),
valid_all_id AS
(
    SELECT r.db_id FROM valid_rupee_id r INNER JOIN valid_other_id o ON r.db_id = o.db_id
),
valid_rupee_tm_score AS
(
    SELECT * FROM all_rupee_tm_score r INNER JOIN valid_all_id v ON v.db_id = r.db_id_1 
),
valid_other AS
(
    SELECT * FROM all_other o INNER JOIN valid_all_id v ON v.db_id = o.db_id_1 
),

-- only look at valid tables from above beyond this point

-- rupee TM-score
ranked_rupee_tm_score AS
(
    SELECT
        r.db_id_1,
        r.n,
        CASE 
            WHEN d1.cl = d2.cl AND d1.cf = d2.cf THEN 1
            ELSE 0
        END AS same_fold
    FROM
        valid_rupee_tm_score r
        INNER JOIN scop_domain d1
            ON d1.scop_id = r.db_id_1
        INNER JOIN scop_domain d2
            ON d2.scop_id = r.db_id_2
),
summed_rupee_tm_score AS
(
    SELECT
        db_id_1,
        SUM(same_fold) AS sum_same_fold
    FROM
        ranked_rupee_tm_score
    GROUP BY
        db_id_1
),

-- other
ranked_other AS
(
    SELECT
        r.n,
        r.db_id_1,
        CASE 
            WHEN d1.cl = d2.cl AND d1.cf = d2.cf THEN 1
            ELSE 0
        END AS same_fold
    FROM
        valid_other r
        INNER JOIN scop_domain d1
            ON d1.scop_id = r.db_id_1
        INNER JOIN scop_domain d2
            ON d2.scop_id = r.db_id_2
),
summed_other AS
(
    SELECT
        db_id_1,
        SUM(same_fold) AS sum_same_fold
    FROM
        ranked_other
    GROUP BY
        db_id_1
)
SELECT
    r.db_id_1 AS db_id,
    CARDINALITY(g.grams) AS len, 
    r.sum_same_fold AS rupee_sum,
    o.sum_same_fold AS other_sum,
    CASE WHEN o.sum_same_fold > r.sum_same_fold + 5 THEN 1 END AS take_a_look
FROM
    summed_rupee_tm_score r 
    INNER JOIN summed_other o
        ON r.db_id_1 = o.db_id_1
    INNER JOIN scop_grams g
        ON g.scop_id = r.db_id_1
ORDER BY
    r.db_id_1;
