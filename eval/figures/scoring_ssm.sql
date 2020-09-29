
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_ssm_d248'; 
    DECLARE p_version VARCHAR := 'casp_scop_v1_73'; 
    DECLARE p_search_type VARCHAR := 'q_score'; -- full_length (with 2), q_score (with 3)
    DECLARE p_sort_by INTEGER := 3; -- 2 (tm_avg_tm_score), 3 (tm_q_score)
    DECLARE p_limit INTEGER := 100; 

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH rupee_all_aligned AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'all_aligned', p_search_type, p_sort_by, p_limit)
        ),
        rupee_top_aligned AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'top_aligned', p_search_type, p_sort_by, p_limit)
        ),
        rupee_fast AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'fast', p_search_type, p_sort_by, p_limit)
        ),
        ssm AS
        (   
            SELECT * FROM get_ssm_results(p_benchmark, p_version, 'q_score', p_sort_by, p_limit) 
        ),
        ranked AS
        (
            SELECT 
                n,
                'RUPEE All-Aligned' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 2 THEN tm_avg_tm_score
                    WHEN p_sort_by = 3 THEN tm_q_score
                END AS score
            FROM
                rupee_all_aligned
            UNION ALL
            SELECT 
                n,
                'RUPEE Top-Aligned' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 2 THEN tm_avg_tm_score
                    WHEN p_sort_by = 3 THEN tm_q_score
                END AS score
            FROM
                rupee_top_aligned
            UNION ALL
            SELECT 
                n,
                'RUPEE Fast' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 2 THEN tm_avg_tm_score
                    WHEN p_sort_by = 3 THEN tm_q_score
                END AS score
            FROM
                rupee_fast
            UNION ALL
            SELECT 
                n,
                'SSM' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 2 THEN tm_avg_tm_score
                    WHEN p_sort_by = 3 THEN tm_q_score
                END AS score
            FROM
                ssm
        ),
        accumulated AS
        (
            SELECT 
                n,
                app,
                AVG(score) OVER (PARTITION BY app, db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS cume_score
            FROM
                ranked
        ),
        averaged AS 
        (
            SELECT
                n,
                app,
                AVG(cume_score) AS avg_cume_score
            FROM
                accumulated 
            GROUP BY
                n,
                app
        )
        SELECT
            *
        FROM
            averaged
        ORDER BY
            app,
            n;

END $$;
    
SELECT * FROM figure_table;

