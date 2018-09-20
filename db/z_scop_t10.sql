
-- scop domains for testing speed
WITH gram_counts AS
(
    SELECT
        ROW_NUMBER() OVER (ORDER BY CARDINALITY(g.grams), g.scop_id) AS n,
        g.scop_id,
        CARDINALITY(g.grams) AS gram_count
    FROM
        scop_grams g
        INNER JOIN benchmark b
            ON b.db_id = g.scop_id
    WHERE
        b.name = 'scop_d62'
)
SELECT
    'scop_t10',
    scop_id
FROM
    gram_counts
WHERE
    (n - 1) % 6 = 0
ORDER BY
    n
LIMIT 10;
