
WITH all_rupee AS
(
    SELECT * FROM get_rupee_results('cath_d94', 'cath_v4_1_0', 50)
),
all_rupee_fast AS
(
    SELECT * FROM get_rupee_results('cath_d94', 'cath_v4_1_0_fast', 50)
),
all_other AS
(   
    SELECT * FROM get_cathedral_results('cath_d94', 'cath_v4_1_0', 50)
),
valid_rupee_id AS
(
    SELECT DISTINCT db_id_1 AS db_id FROM all_rupee
),
valid_rupee_fast_id AS
(
    SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_fast
),
valid_other_id AS
(
    SELECT DISTINCT db_id_1 AS db_id FROM all_other
),
valid_all_id AS
(
    SELECT r.db_id FROM valid_rupee_id r INNER JOIN valid_rupee_fast_id f ON r.db_id = f.db_id INNER JOIN valid_other_id o ON r.db_id = o.db_id
),
valid_rupee AS
(
    SELECT * FROM all_rupee r INNER JOIN valid_all_id v ON v.db_id = r.db_id_1 
),
valid_rupee_fast AS
(
    SELECT * FROM all_rupee_fast f INNER JOIN valid_all_id v ON v.db_id = f.db_id_1 
),
valid_other AS
(
    SELECT * FROM all_other o INNER JOIN valid_all_id v ON v.db_id = o.db_id_1 
),
ranked AS
(
    -- tm score

    SELECT 
        n,
        'RUPEE' AS app,
        'CE' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        ce_tm_score AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        n,
        'RUPEE' AS app,
        'FATCAT' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        fatcat_tm_score AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        n,
        'RUPEE Fast' AS app,
        'CE' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        ce_tm_score AS score
    FROM
        valid_rupee_fast
    UNION ALL
    SELECT 
        n,
        'RUPEE Fast' AS app,
        'FATCAT' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        fatcat_tm_score AS score
    FROM
        valid_rupee_fast
    UNION ALL
    SELECT 
        n,
        'OTHER' AS app,
        'CE' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        ce_tm_score AS score
    FROM
        valid_other
    UNION ALL
    SELECT 
        n,
        'OTHER' AS app,
        'FATCAT' AS alg,
        'TM-Score' AS score_type,
        db_id_1,
        fatcat_tm_score AS score
    FROM
        valid_other

    -- rmsd
    
    UNION ALL 
    SELECT 
        n, 
        'RUPEE' AS app,
        'CE' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        ce_rmsd AS score
    FROM
        valid_rupee
    UNION ALL
    SELECT 
        n,
        'RUPEE' AS app,
        'FATCAT' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        fatcat_rmsd AS score
    FROM
        valid_rupee
    UNION ALL 
    SELECT 
        n, 
        'RUPEE Fast' AS app,
        'CE' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        ce_rmsd AS score
    FROM
        valid_rupee_fast
    UNION ALL
    SELECT 
        n,
        'RUPEE Fast' AS app,
        'FATCAT' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        fatcat_rmsd AS score
    FROM
        valid_rupee_fast
    UNION ALL
    SELECT 
        n,
        'OTHER' AS app,
        'CE' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        ce_rmsd AS score
    FROM
        valid_other
    UNION ALL
    SELECT 
        n,
        'OTHER' AS app,
        'FATCAT' AS alg,
        'RMSD' AS score_type,
        db_id_1,
        fatcat_rmsd AS score
    FROM
        valid_other
),
accumulated AS
(
    SELECT 
        n,
        app,
        alg,
        score_type,
        AVG(score) OVER (PARTITION BY app, alg, score_type, db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS cume_score
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
