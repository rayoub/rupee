
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
)
SELECT
    benchmark,
    app,
    db_id,
    timing,
    n,
    gram_count
FROM
    scop_d62_timing
ORDER BY
    benchmark,
    app,
    n;


