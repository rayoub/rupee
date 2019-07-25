
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_cathedral_d149'; -- casp_mtm_d34, casp_mtm_d144, casp_cathedral_d149, and ssm benchmark to follow
    DECLARE p_version VARCHAR := 'casp_cath_v4_2_0'; -- casp_scop_v1_73, casp_cath_v4_2_0, casp_chain_v06_26_2019
    DECLARE p_limit INTEGER := 100; -- 10, 100
    DECLARE p_other VARCHAR := 'CATHEDRAL'; -- mTM, CATHEDRAL, SSM
    DECLARE p_alg VARCHAR = 'TM_AVG'; -- TM_Q, TM_AVG

    -- don't forget to change get_*_results as needed

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH all_rupee_all_aligned AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'all_aligned', p_limit)
        ),
        all_other AS
        (   
            SELECT * FROM get_cathedral_results(p_benchmark, p_version, p_limit)
        ),
        valid_rupee_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_all_aligned
            --UNION 
        ),
        valid_other_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM all_other
        ),
        valid_all_id AS
        (
            SELECT r.db_id FROM valid_rupee_id r INNER JOIN valid_other_id o ON r.db_id = o.db_id
        ),
        valid_rupee_all_aligned AS
        (
            SELECT * FROM all_rupee_all_aligned r INNER JOIN valid_all_id v ON v.db_id = r.db_id_1 
        ),
        valid_other AS
        (
            SELECT * FROM all_other o INNER JOIN valid_all_id v ON v.db_id = o.db_id_1 
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
