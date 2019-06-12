
DO $$

    DECLARE p_benchmark VARCHAR := 'scop_d360';
    DECLARE p_version VARCHAR := 'scop_v2_07';
    DECLARE p_limit INTEGER := 100; 
    DECLARE p_alg VARCHAR = 'TM_AVG'; 

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH rupee_1 AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'tm_score_1', p_limit)
        ),
        rupee_2 AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'tm_score_2', p_limit)
        ),
        ranked AS
        (
            SELECT 
                n,
                'RUPEE 1' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score WHEN p_alg = 'TM_Q' THEN tm_q_tm_score WHEN p_alg = 'TM_AVG' THEN tm_avg_tm_score ELSE fatcat_tm_score END AS score
            FROM
                rupee_1
            UNION ALL
            SELECT 
                n,
                'RUPEE 2' AS app,
                p_alg AS alg,
                'TM-Score' AS score_type,
                db_id_1,
                CASE WHEN p_alg = 'CE' THEN ce_tm_score WHEN p_alg = 'TM_Q' THEN tm_q_tm_score WHEN p_alg = 'TM_AVG' THEN tm_avg_tm_score ELSE fatcat_tm_score END AS score
            FROM
                rupee_2
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
