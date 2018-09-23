
DO $$

    DECLARE p_benchmark VARCHAR := 'cath_d94';
    DECLARE p_version VARCHAR := 'cath_v4_1_0';
    DECLARE p_limit INTEGER := 50;
    DECLARE p_other VARCHAR := 'CATHEDRAL';

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
            SELECT * FROM get_cathedral_results(p_benchmark, p_version, p_limit)
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
        ranked_rupee AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t THEN 1
                    ELSE 0
                END AS same_topology
            FROM
                valid_rupee r
                INNER JOIN cath_domain d1
                    ON d1.cath_id = r.db_id_1
                INNER JOIN cath_domain d2
                    ON d2.cath_id = r.db_id_2
        ),
        summed_rupee AS
        (
            SELECT
                n,
                SUM(same_topology) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_topology
            FROM
                ranked_rupee
        ),
        totaled_rupee AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_topology) AS total_same_topology
            FROM
                summed_rupee
            GROUP BY
                n
        ),
        average_rupee AS
        (
            SELECT
                n,
                total_n,
                'RUPEE'::TEXT AS app,
                total_same_topology::REAL / total_n AS level_precision
            FROM
                totaled_rupee
        ),
        ranked_rupee_fast AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t THEN 1
                    ELSE 0
                END AS same_topology
            FROM
                valid_rupee_fast r
                INNER JOIN cath_domain d1
                    ON d1.cath_id = r.db_id_1
                INNER JOIN cath_domain d2
                    ON d2.cath_id = r.db_id_2
        ),
        summed_rupee_fast AS
        (
            SELECT
                n,
                SUM(same_topology) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_topology
            FROM
                ranked_rupee_fast
        ),
        totaled_rupee_fast AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_topology) AS total_same_topology
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
                total_same_topology::REAL / total_n AS level_precision
            FROM
                totaled_rupee_fast
        ),
        ranked_other AS
        (
            SELECT
                r.n,
                r.db_id_1,
                CASE 
                    WHEN d1.c = d2.c AND d1.a = d2.a AND d1.t = d2.t THEN 1
                    ELSE 0
                END AS same_topology
            FROM
                valid_other r
                INNER JOIN cath_domain d1
                    ON d1.cath_id = r.db_id_1
                INNER JOIN cath_domain d2
                    ON d2.cath_id = r.db_id_2
        ),
        summed_other AS
        (
            SELECT
                n,
                SUM(same_topology) OVER (PARTITION BY db_id_1 ORDER BY n ROWS UNBOUNDED PRECEDING) AS sum_same_topology
            FROM
                ranked_other
        ),
        totaled_other AS
        (
            SELECT
                n,
                COUNT(*) * n AS total_n, 
                SUM(sum_same_topology) AS total_same_topology
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
                total_same_topology::REAL / total_n AS level_precision
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
                average_rupee
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
