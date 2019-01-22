
DO $$

    DECLARE p_benchmark VARCHAR := 'scop_d360'; -- scop_d360, scop_d62, or cath_d99
    DECLARE p_version VARCHAR := 'scop_v2_07'; -- scop_v2_07, scop_v1_73, or cath_v4_2_0
    DECLARE p_limit INTEGER := 100;
    DECLARE p_other VARCHAR := 'mTM'; -- mTM, SSM, or CATHEDRAL
    DECLARE p_alg VARCHAR = 'FATCAT'; -- CE, FATCAT, or TM

    -- don't forget to change get_*_results as needed

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH all_rupee_tm_score AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'tm_score', p_limit)
        ),
        all_rupee_rmsd AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'rmsd', p_limit)
        ),
        all_rupee_fast AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'similarity', p_limit)
        ),
        all_other AS
        (   
            SELECT * FROM get_mtm_dom_results(p_benchmark, p_version, p_limit)
        ),
        valid_rupee_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_tm_score
            UNION 
            SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_rmsd
            UNION
            SELECT DISTINCT db_id_1 AS db_id FROM all_other
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
        valid_rupee_rmsd AS
        (
            SELECT * FROM all_rupee_rmsd r INNER JOIN valid_all_id v ON v.db_id = r.db_id_1 
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
            SELECT 
                n,
                'RUPEE TM-Score' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score WHEN p_alg = 'TM' THEN tm_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_rupee_tm_score
            UNION ALL
            SELECT 
                n,
                'RUPEE Fast' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score WHEN p_alg = 'TM' THEN tm_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_rupee_fast
            UNION ALL
            SELECT 
                n,
                p_other AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score WHEN p_alg = 'TM' THEN tm_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_other
            UNION ALL 
            SELECT 
                n, 
                'RUPEE RMSD' AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd WHEN p_alg = 'TM' THEN tm_rmsd ELSE fatcat_rmsd END AS score
            FROM
                valid_rupee_rmsd
            UNION ALL
            SELECT 
                n, 
                'RUPEE Fast' AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd WHEN p_alg = 'TM' THEN tm_rmsd ELSE fatcat_rmsd END AS score
            FROM
                valid_rupee_fast
            UNION ALL
            SELECT 
                n,
                p_other AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd WHEN p_alg = 'TM' THEN tm_rmsd ELSE fatcat_rmsd END AS score
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

END $$;

SELECT * FROM figure_table;
