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
ranked AS
(
    SELECT 
        -- the secondary sort must not be an rmsd or tm-score since those are assumed not available for search in general
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY rupee_score DESC, pdb_id_2) AS n,
        'RUPEE' AS app,
        'CE-CP' AS alg,
        'TM-Score' AS score_type,
        pdb_id_1,
        cecp_tm_score AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY rupee_score DESC, pdb_id_2) AS n,
        'RUPEE' AS app,
        'FATCAT' AS alg,
        'TM-Score' AS score_type,
        pdb_id_1,
        fatcat_tm_score AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY ssap_score DESC) AS n,
        'CATHEDRAL' AS app,
        'CE-CP' AS alg,
        'TM-Score' AS score_type,
        pdb_id_1,
        cecp_tm_score AS score
    FROM
        valid_cath
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY ssap_score DESC) AS n,
        'CATHEDRAL' AS app,
        'FATCAT' AS alg,
        'TM-Score' AS score_type,
        pdb_id_1,
        fatcat_tm_score AS score
    FROM
        valid_cath
    UNION ALL 
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY rupee_score DESC, pdb_id_2) AS n,
        'RUPEE' AS app,
        'CE-CP' AS alg,
        'RMSD' AS score_type,
        pdb_id_1,
        cecp_rmsd AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY rupee_score DESC, pdb_id_2) AS n,
        'RUPEE' AS app,
        'FATCAT' AS alg,
        'RMSD' AS score_type,
        pdb_id_1,
        fatcat_rmsd AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY ssap_score DESC) AS n,
        'CATHEDRAL' AS app,
        'CE-CP' AS alg,
        'RMSD' AS score_type,
        pdb_id_1,
        cecp_rmsd AS score
    FROM
        valid_cath
    UNION ALL
    SELECT 
        ROW_NUMBER() OVER (PARTITION BY pdb_id_1 ORDER BY ssap_score DESC) AS n,
        'CATHEDRAL' AS app,
        'FATCAT' AS alg,
        'RMSD' AS score_type,
        pdb_id_1,
        fatcat_rmsd AS score
    FROM
        valid_cath
),
accumulated AS
(
    SELECT 
        n,
        app,
        alg,
        score_type,
        AVG(score) OVER (PARTITION BY app, alg, score_type, pdb_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS cume_score
    FROM
        ranked
),
averaged AS 
(
    SELECT
        n,
        app,
        alg,
        score_type,
        AVG(cume_score) AS avg_cume_score
    FROM
        accumulated 
    GROUP BY
        n,
        app,
        alg,
        score_type
)
SELECT
    *
FROM
    averaged
ORDER BY
    score_type,
    app,
    alg,
    n;

