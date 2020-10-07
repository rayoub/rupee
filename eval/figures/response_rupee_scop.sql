
WITH rupee_response AS
(
    SELECT
        rt.search_mode AS app,
        ROW_NUMBER() OVER (PARTITION BY rt.search_mode ORDER BY CARDINALITY(g.grams), rt.db_id) AS n,
        rt.db_id,
        rt.time,
        CARDINALITY(g.grams) AS residue_count
    FROM
        rupee_time rt
        INNER JOIN benchmark b
            ON rt.db_id = b.db_id
        INNER JOIN scop_grams g
            ON rt.db_id = g.scop_id
    WHERE
        b.name = 'scop_d235'
        AND rt.version = 'scop_v2_07'
        AND rt.search_type = 'full_length'
)
SELECT 
    CASE 
        WHEN app = 'all_aligned' THEN 'All'
        WHEN app = 'top_aligned' THEN 'Top'
        WHEN app = 'fast' THEN 'Fast'
        WHEN app = 'optimal' THEN 'Exhaustive'
    END AS app,
    n,
    db_id,
    (time / 1000)::INTEGER AS time,
    residue_count
FROM 
    rupee_response
ORDER BY
    app,
    n;

