
DO $$

    DECLARE p_benchmark VARCHAR := 'scop_d62'; -- scop_d360 or scop_d62
    DECLARE p_version VARCHAR := 'scop_v1_73'; -- scop_v2_07 or scop_v1_73
    DECLARE p_limit INTEGER := 50; -- 100 or 50
    DECLARE p_other VARCHAR := 'SSM'; -- mTM or SSM

    -- don't forget to change get_*_results as needed

BEGIN

    DROP TABLE IF EXISTS figure_table;
   
    CREATE TABLE figure_table AS 
        WITH all_rupee_tm_score AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'tm_score', p_limit)
        ),
        all_rupee_fast AS
        (
            SELECT * FROM get_rupee_results(p_benchmark, p_version, 'similarity', p_limit)
        ),
        all_other AS
        (   
            SELECT * FROM get_ssm_results(p_benchmark, p_version, p_limit)
        ),
        valid_rupee_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_tm_score
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
        valid_rupee_fast AS
        (
            SELECT * FROM all_rupee_fast f INNER JOIN valid_all_id v ON v.db_id = f.db_id_1 
        ),
        valid_other AS
        (
            SELECT * FROM all_other o INNER JOIN valid_all_id v ON v.db_id = o.db_id_1 
        ),

        -- rupee TM-score
        ranked_rupee_tm_score AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.cl = d2.cl AND d1.cf = d2.cf THEN 1
                    ELSE 0
                END AS same_fold
            FROM
                valid_rupee_tm_score r
                INNER JOIN scop_domain d1
                    ON d1.scop_id = r.db_id_1
                INNER JOIN scop_domain d2
                    ON d2.scop_id = r.db_id_2
        ),
        summed_rupee_tm_score AS
        (
            SELECT
                n,
                SUM(same_fold) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_fold
            FROM
                ranked_rupee_tm_score
        ),
        totaled_rupee_tm_score AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_fold) AS total_same_fold
            FROM
                summed_rupee_tm_score
            GROUP BY
                n
        ),
        average_rupee_tm_score AS
        (
            SELECT
                n,
                total_n,
                'RUPEE'::TEXT AS app,
                total_same_fold::REAL / total_n AS level_precision
            FROM
                totaled_rupee_tm_score
        ),

        -- rupee fast
        ranked_rupee_fast AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.cl = d2.cl AND d1.cf = d2.cf THEN 1
                    ELSE 0
                END AS same_fold
            FROM
                valid_rupee_fast r
                INNER JOIN scop_domain d1
                    ON d1.scop_id = r.db_id_1
                INNER JOIN scop_domain d2
                    ON d2.scop_id = r.db_id_2
        ),
        summed_rupee_fast AS
        (
            SELECT
                n,
                SUM(same_fold) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_fold
            FROM
                ranked_rupee_fast
        ),
        totaled_rupee_fast AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_fold) AS total_same_fold
            FROM
                summed_rupee_fast
            GROUP BY
                n
        ),
        average_rupee_fast AS
        (
            SELECT
                n,
                total_n,
                'RUPEE Fast'::TEXT AS app,
                total_same_fold::REAL / total_n AS level_precision
            FROM
                totaled_rupee_fast
        ),

        -- other
        ranked_other AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.cl = d2.cl AND d1.cf = d2.cf THEN 1
                    ELSE 0
                END AS same_fold
            FROM
                valid_other r
                INNER JOIN scop_domain d1
                    ON d1.scop_id = r.db_id_1
                INNER JOIN scop_domain d2
                    ON d2.scop_id = r.db_id_2
        ),
        summed_other AS
        (
            SELECT
                n,
                SUM(same_fold) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_fold
            FROM
                ranked_other
        ),
        totaled_other AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_fold) AS total_same_fold
            FROM
                summed_other
            GROUP BY
                n
        ),
        average_other AS
        (
            SELECT
                n,
                total_n,
                p_other::TEXT AS app,
                total_same_fold::REAL / total_n AS level_precision
            FROM
                totaled_other
        ),
        average AS 
        (
            SELECT
                n,
                app,
                level_precision
            FROM
                average_rupee_tm_score
            UNION ALL
            SELECT
                n,
                app,
                level_precision
            FROM
                average_rupee_fast
            UNION ALL
            SELECT
                n,
                app,
                level_precision
            FROM
                average_other
        )
        SELECT
            *
        FROM 
            average
        ORDER BY
            app,
            n;

END $$;

SELECT * FROM figure_table;
