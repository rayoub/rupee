
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_cathedral_d247'; 
    DECLARE p_version VARCHAR := 'casp_cath_v4_2_0'; -- casp_chain_v01_01_2020, casp_scop_v1_73, casp_cath_v4_2_0
    DECLARE p_limit INTEGER := 100; 
    DECLARE p_other VARCHAR := 'CATHEDRAL'; -- mTM, SSM, CATHEDRAL
    DECLARE p_alg VARCHAR = 'TM_AVG'; -- TM_Q, TM_AVG

    -- don't forget to change get_*_results as needed

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH rupee_all_aligned AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'all_aligned', p_limit)
        ),
        rupee_top_aligned AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'top_aligned', p_limit)
        ),
        other AS
        (   
            SELECT * FROM get_cathedral_results(p_benchmark, p_version, p_limit)
        ),
        valid_rupee_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM rupee_all_aligned
            UNION
            SELECT DISTINCT db_id_1 AS db_id FROM rupee_top_aligned
        ),
        valid_other_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM other
        ),
        valid_id AS
        (
            -- during data acquisition we need to make sure we favor 'other' when it comes to mismatches
            SELECT r.db_id FROM valid_rupee_id r INNER JOIN valid_other_id o ON r.db_id = o.db_id
        ),
        valid_rupee_all_aligned AS
        (
            SELECT * FROM rupee_all_aligned r INNER JOIN valid_id v ON v.db_id = r.db_id_1 
        ),
        valid_rupee_top_aligned AS
        (
            SELECT * FROM rupee_top_aligned r INNER JOIN valid_id v ON v.db_id = r.db_id_1 
        ),
        valid_other AS
        (
            SELECT * FROM other o INNER JOIN valid_id v ON v.db_id = o.db_id_1 
        ),
        ranked AS
        (
            SELECT 
                n,
                'RUPEE All-Aligned' AS app,
                p_alg AS alg,
                db_id_1,
                CASE WHEN p_alg = 'TM_Q' THEN tm_q_tm_score ELSE tm_avg_tm_score END AS score
            FROM
                valid_rupee_all_aligned
            UNION ALL
            SELECT 
                n,
                'RUPEE Top-Aligned' AS app,
                p_alg AS alg,
                db_id_1,
                CASE WHEN p_alg = 'TM_Q' THEN tm_q_tm_score ELSE tm_avg_tm_score END AS score
            FROM
                valid_rupee_top_aligned
            UNION ALL
            SELECT 
                n,
                p_other AS app,
                p_alg AS alg,
                db_id_1,
                CASE WHEN p_alg = 'TM_Q' THEN tm_q_tm_score ELSE tm_avg_tm_score END AS score
            FROM
                valid_other
        ),
        accumulated AS
        (
            SELECT 
                n,
                app,
                alg,
                AVG(score) OVER (PARTITION BY app, alg, db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS cume_score
            FROM
                ranked
        ),
        averaged AS 
        (
            SELECT
                n,
                app,
                alg,
                AVG(cume_score) AS avg_cume_score
            FROM
                accumulated 
            GROUP BY
                n,
                app,
                alg
        )
        SELECT
            *
        FROM
            averaged
        ORDER BY
            app,
            alg,
            n;

END $$;
    
SELECT * FROM figure_table;

