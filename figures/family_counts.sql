
WITH family_counts AS
(
    SELECT
        d.cath,
        COUNT(*) AS family_count
    FROM
        cath_domain d
        INNER JOIN cath_diverse_family f
            ON f.cath = d.cath
    WHERE
        d.solid LIKE '%1.1.1.1' -- restriction to s35 representatives
    GROUP BY
        d.cath
)
SELECT
    MIN(family_count) AS min_family_count,
    MAX(family_count) AS max_family_count,
    AVG(family_count) AS avg_family_count
FROM
    family_counts;
    

