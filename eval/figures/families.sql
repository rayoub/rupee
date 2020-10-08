
-- i wonder if we do this for mtm we will probably see big diffrences.
-- also, consider checking diversity of results. perhaps that is what puts rupee over the top 

-- TODO: Thursday read through the full clean paper and make corrections. Then begin rebuttal for what has been successfully addressed. 


-- RUPEE counts across top 1 results
WITH top_results AS
(
    SELECT
        SUBSTRING(r.db_id_1 FROM 1 FOR 7) AS prefix,
        SUBSTRING(r.db_id_1 FROM CHAR_LENGTH(r.db_id_1) - 1 FOR 2) AS suffix,
        r.db_id_1 AS db_id,
        r.db_id_2 AS neighbor,
        d.cl_cf_sf
    FROM
        rupee_result r
        INNER JOIN scop_domain d
            ON d.scop_id = r.db_id_2
    WHERE
        r.version = 'casp_scop_v2_07'
        AND r.search_mode = 'all_aligned'
        AND r.n = 1
),
keyed_counts AS 
(
    SELECT
        t.prefix || t.suffix AS key,
        COUNT(DISTINCT t.cl_cf_sf) AS superfamily_count
    FROM
        top_results t
    GROUP BY
        t.prefix,
        t.suffix
    ORDER BY
        COUNT(DISTINCT t.cl_cf_sf)
)
SELECT
    superfamily_count,
    COUNT(*) AS target_count
FROM
    keyed_counts
GROUP BY
    superfamily_count
ORDER BY
    superfamily_count
;
