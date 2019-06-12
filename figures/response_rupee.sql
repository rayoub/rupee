
WITH scop_d62_timing AS
(
    SELECT
        t.benchmark,
        t.app,
        t.db_id,
        t.timing,
        ROW_NUMBER() OVER (PARTITION BY t.benchmark, t.app ORDER BY CARDINALITY(g.grams)) AS n,
        CARDINALITY(g.grams) AS gram_count
    FROM
        response_time t
        INNER JOIN scop_grams g
            ON g.scop_id = t.db_id
            AND t.benchmark = 'scop_d62'
    WHERE
        t.app IN ('mtm', 'ssm', 'rupee', 'rupee_2')
)
SELECT
    benchmark,
    CASE 
        WHEN app = 'mtm' THEN 'mTM'
        WHEN app = 'ssm' THEN 'SSM'
        WHEN app = 'rupee' THEN 'RUPEE Top-Aligned'
        WHEN app = 'rupee_2' THEN 'RUPEE Test'
    END AS app,
    db_id,
    timing / 1000 AS timing,
    n,
    gram_count
FROM
    scop_d62_timing
ORDER BY
    benchmark,
    app,
    n;


