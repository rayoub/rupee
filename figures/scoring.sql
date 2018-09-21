
DO $$

    DECLARE p_benchmark VARCHAR := 'scop_d360';
    DECLARE p_version VARCHAR := 'scop_v2_07';
    DECLARE p_limit INTEGER := 100;
    DECLARE p_other VARCHAR := 'mTM';
    DECLARE p_alg VARCHAR = 'CE'; -- CE or FATCAT

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH all_rupee AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, p_limit)
        ),
        all_rupee_fast AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version || '_fast', p_limit)
        ),
        all_other AS
        (   
            SELECT * FROM get_mtm_dom_results(p_benchmark, p_version, p_limit)
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
            SELECT 
                n,
                'RUPEE' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_rupee
            UNION ALL
            SELECT 
                n,
                'RUPEE Fast' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_rupee_fast
            UNION ALL
            SELECT 
                n,
                p_other AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score ELSE fatcat_tm_score END AS score
            FROM
                valid_other
            UNION ALL 
            SELECT 
                n, 
                'RUPEE' AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd ELSE fatcat_rmsd END AS score
            FROM
                valid_rupee
            UNION ALL
            SELECT 
                n, 
                'RUPEE Fast' AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd ELSE fatcat_rmsd END AS score
            FROM
                valid_rupee_fast
            UNION ALL
            SELECT 
                n,
                p_other AS app,
                p_alg AS alg,
                'RMSD' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_rmsd ELSE fatcat_rmsd END AS score
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
