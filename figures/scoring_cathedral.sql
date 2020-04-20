
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_cathedral_d247'; 
    DECLARE p_version VARCHAR := 'casp_cath_v4_2_0'; 
    DECLARE p_search_type VARCHAR := 'full_length'; -- rmsd, full_length, ssap_score
    DECLARE p_sort_by INTEGER := 4; -- 1 (ce_rmsd), 2 (fatcat_rigid_rmsd), 4 (tm_avg_tm_score), 7 (ssap_score)
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
        cathedral AS
        (   
            SELECT * FROM get_cathedral_results(p_benchmark, p_version, p_sort_by, p_limit) 
        ),
        ranked AS
        (
            SELECT 
                n,
                'RUPEE All-Aligned' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 1 THEN ce_rmsd 
                    WHEN p_sort_by = 2 THEN fatcat_rigid_rmsd
                    WHEN p_sort_by = 4 THEN tm_avg_tm_score
                    WHEN p_sort_by = 7 THEN ssap_score
                END AS score
            FROM
                rupee_all_aligned
            UNION ALL
            SELECT 
                n,
                'RUPEE Top-Aligned' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 1 THEN ce_rmsd 
                    WHEN p_sort_by = 2 THEN fatcat_rigid_rmsd
                    WHEN p_sort_by = 4 THEN tm_avg_tm_score
                    WHEN p_sort_by = 7 THEN ssap_score
                END AS score
            FROM
                rupee_top_aligned
            UNION ALL
            SELECT 
                n,
                'CATHEDRAL' AS app,
                db_id_1,
                CASE 
                    WHEN p_sort_by = 1 THEN ce_rmsd 
                    WHEN p_sort_by = 2 THEN fatcat_rigid_rmsd
                    WHEN p_sort_by = 4 THEN tm_avg_tm_score
                    WHEN p_sort_by = 7 THEN ssap_score
                END AS score
            FROM
                cathedral
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
