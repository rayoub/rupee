
        WITH all_rupee_tm_score AS
        (
            SELECT * FROM get_rupee_results('scop_d360', 'scop_v2_07', 'tm_score', 100)
        ),
        all_other AS
        (   
            SELECT * FROM get_mtm_dom_results('scop_d360', 'scop_v2_07', 100)
        ),
        valid_rupee_id AS
        (
            SELECT DISTINCT db_id_1 AS db_id FROM all_rupee_tm_score
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
        valid_other AS
        (
            SELECT * FROM all_other o INNER JOIN valid_all_id v ON v.db_id = o.db_id_1 
        ),

        -- rupee TM-score
        rupee_lens AS
        (
            SELECT
                r.n,
                r.db_id_1,
                r.db_id_2,
                CARDINALITY(g1.grams) AS len1,
                CARDINALITY(g2.grams) AS len2,
                ABS(CARDINALITY(g1.grams) - CARDINALITY(g2.grams)) AS len_diff,
                ABS(CARDINALITY(g1.grams) - CARDINALITY(g2.grams)) / CARDINALITY(g1.grams)::REAL AS pct_diff
            FROM
                valid_rupee_tm_score r
                INNER JOIN scop_grams g1
                    ON g1.scop_id = r.db_id_1
                INNER JOIN scop_grams g2
                    ON g2.scop_id = r.db_id_2
            ORDER BY
                r.db_id_1,
                r.n
        ),
        max_rupee_diffs AS
        (
            SELECT
                db_id_1,
                MAX(pct_diff) AS max_pct_diff
            FROM
                rupee_lens
            GROUP BY
                db_id_1
            ORDER BY
                db_id_1
        ),
        
        -- other
        other_lens AS
        (
            SELECT
                r.n,
                r.db_id_1,
                r.db_id_2,
                CARDINALITY(g1.grams) AS len1,
                CARDINALITY(g2.grams) AS len2,
                ABS(CARDINALITY(g1.grams) - CARDINALITY(g2.grams)) AS len_diff,
                ABS(CARDINALITY(g1.grams) - CARDINALITY(g2.grams)) / CARDINALITY(g1.grams)::REAL AS pct_diff
            FROM
                valid_other r
                INNER JOIN scop_grams g1
                    ON g1.scop_id = r.db_id_1
                INNER JOIN scop_grams g2
                    ON g2.scop_id = r.db_id_2
            ORDER BY
                r.db_id_1,
                r.n
        ),
        max_other_diffs AS
        (
            SELECT
                db_id_1,
                MAX(pct_diff) AS max_pct_diff
            FROM
                other_lens
            GROUP BY
                db_id_1
            ORDER BY
                db_id_1
        ),
       
        -- for comparing individually over benchmark

        compare_diffs AS
        (
            SELECT
                r.db_id_1,
                r.max_pct_diff AS rupee_max_diff,
                o.max_pct_diff AS other_max_diff
            FROM 
                max_rupee_diffs r
                INNER JOIN max_other_diffs o
                    ON o.db_id_1 = r.db_id_1
            ORDER BY
                r.db_id_1
        ),
        
        -- for comparing averages over benchmark

        compare_avgs AS
        (
            SELECT
                AVG(rupee_max_diff) AS rupee_avg_max_diff,
                AVG(other_max_diff) AS other_avg_max_diff
            FROM
                compare_diffs
        )        

        SELECT * FROM compare_avgs;



