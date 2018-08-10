
WITH all_rupee AS
(
    SELECT * FROM get_rupee_results(50)
),
all_cath AS
(   
    SELECT * FROM get_cath_results(50)
),
valid_rupee_id AS
(
    SELECT DISTINCT pdb_id_1 AS pdb_id FROM all_rupee
),
valid_cath_id AS
(
    SELECT DISTINCT pdb_id_1 AS pdb_id FROM all_cath
),
valid_both_id AS
(
    SELECT b.pdb_id FROM valid_rupee_id b INNER JOIN valid_cath_id c ON b.pdb_id = c.pdb_id
),
valid_rupee AS
(
    SELECT * FROM all_rupee b INNER JOIN valid_both_id v ON v.pdb_id = b.pdb_id_1 
),
valid_cath AS
(
    SELECT * FROM all_cath c INNER JOIN valid_both_id v ON v.pdb_id = c.pdb_id_1 
),
ranked_rupee AS
(
    SELECT
        r.n,
        r.pdb_id_1,
        CASE 
            WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t THEN 1
            ELSE 0
        END AS same_topology,
        CASE 
            WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t AND d1.h = d2.h THEN 1
            ELSE 0
        END AS same_family
    FROM
        valid_rupee r
        INNER JOIN cath_domain_stage d1
            ON d1.pdb_id = r.pdb_id_1
        INNER JOIN cath_domain_stage d2
            ON d2.pdb_id = r.pdb_id_2
),
summed_rupee AS
(
    SELECT
        n,
        SUM(same_topology) OVER (PARTITION BY pdb_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_topology,
        SUM(same_family) OVER (PARTITION BY pdb_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_family
    FROM
        ranked_rupee
),
totaled_rupee AS
(
    SELECT
        n,
        COUNT(*) * n AS total_n, 
        SUM(sum_same_topology) AS total_same_topology,
        SUM(sum_same_family) AS total_same_family
    FROM
        summed_rupee
    GROUP BY
        n
),
average_rupee AS
(
    SELECT
        n,
        total_n,
        'RUPEE'::TEXT AS app,
        'Topology'::TEXT AS hierarchy_level,
        total_same_topology::REAL / total_n AS level_precision
    FROM
        totaled_rupee
    UNION ALL
    SELECT
        n,
        total_n,
        'RUPEE'::TEXT AS app,
        'Superfamily'::TEXT AS hierarchy_level,
        total_same_family::REAL / total_n AS level_precision
    FROM
        totaled_rupee
),
ranked_cath AS
(
    SELECT
        r.n,
        r.pdb_id_1,
        CASE 
            WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t THEN 1
            ELSE 0
        END AS same_topology,
        CASE 
            WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t AND d1.h = d2.h THEN 1
            ELSE 0
        END AS same_family
    FROM
        valid_cath r
        INNER JOIN cath_domain_stage d1
            ON d1.pdb_id = r.pdb_id_1
        INNER JOIN cath_domain_stage d2
            ON d2.pdb_id = r.pdb_id_2
),
summed_cath AS
(
    SELECT
        n,
        SUM(same_topology) OVER (PARTITION BY pdb_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_topology,
        SUM(same_family) OVER (PARTITION BY pdb_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_family
    FROM
        ranked_cath
),
totaled_cath AS
(
    SELECT
        n,
        COUNT(*) * n AS total_n, 
        SUM(sum_same_topology) AS total_same_topology,
        SUM(sum_same_family) AS total_same_family
    FROM
        summed_cath
    GROUP BY
        n
),
average_cath AS
(
    SELECT
        n,
        total_n,
        'CATHEDRAL'::TEXT AS app,
        'Topology'::TEXT AS hierarchy_level,
        total_same_topology::REAL / total_n AS level_precision
    FROM
        totaled_cath
    UNION ALL
    SELECT
        n,
        total_n,
        'CATHEDRAL'::TEXT AS app,
        'Superfamily'::TEXT AS hierarchy_level,
        total_same_family::REAL / total_n AS level_precision
    FROM
        totaled_cath
),
average AS 
(
    SELECT
        n,
        app,
        hierarchy_level,
        level_precision
    FROM
        average_rupee
    UNION ALL
    SELECT
        n,
        app,
        hierarchy_level,
        level_precision
    FROM
        average_cath
)
SELECT
    *
FROM 
    average
ORDER BY
    app,
    hierarchy_level,
    n;



