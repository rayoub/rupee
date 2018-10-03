
WITH grams AS (
    SELECT
        g.scop_id,
        UNNEST(g.grams) % 10000 AS gram
    FROM
        scop_grams g
),
gram_counts AS (
    SELECT
        g.gram,
        COUNT(*) AS gram_count
    FROM
        grams g
    GROUP BY
        g.gram
    ORDER BY
        COUNT(*) DESC
),
gram_probs AS (
    SELECT
        gram,
        gram_count / (SELECT SUM(gram_count) FROM gram_counts) AS probability
    FROM
        gram_counts
)
INSERT INTO scop_background (gram_1, gram_2, probability)
SELECT
    p1.gram AS gram_1,
    p2.gram AS gram_2,
    p1.probability * p2.probability AS probability
FROM
    gram_probs p1
    INNER JOIN gram_probs p2
        ON p1.gram <= p2.gram
WHERE
    p1.probability * p2.probability >= 0.00001

ORDER BY
    p1.gram,
    p2.gram;





