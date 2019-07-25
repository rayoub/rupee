
DO $$

    DECLARE p_benchmark VARCHAR := 'casp_d150'; -- scop_d360, casp_d150
    DECLARE p_version VARCHAR := 'casp_scop_v2_07'; -- scop_v2_07, casp_scop_v2_07
    DECLARE p_limit INTEGER := 100; 

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH rupee_1 AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'all_aligned', p_limit)
        ),
        rupee_2 AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'top_aligned', p_limit)
        ),
        ranked AS
        (
            SELECT 
                n,
                'RUPEE All-Aligned' AS app,
                db_id_1,
                tm_avg_tm_score AS score
            FROM
                rupee_1
            UNION ALL
            SELECT 
                n,
                'RUPEE Top-Aligned' AS app,
                db_id_1,
                tm_avg_tm_score AS score
            FROM
                rupee_2
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
