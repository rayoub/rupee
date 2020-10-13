
-- i wonder if we do this for mtm we will probably see big diffrences.
-- also, consider checking diversity of results. perhaps that is what puts rupee over the top 

-- TODO: Thursday read through the full clean paper and make corrections. Then begin rebuttal for what has been successfully addressed. 


-- RUPEE counts across top 1 results
WITH target_superfamilies AS
(
    SELECT
        SUBSTRING(r.db_id_1 FROM 1 FOR 7) || SUBSTRING(r.db_id_1 FROM CHAR_LENGTH(r.db_id_1) - 1 FOR 2) AS target,
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
superfamily_counts AS 
(
    SELECT
        target,
        COUNT(DISTINCT cl_cf_sf) AS superfamily_count
    FROM 
        target_superfamilies
    GROUP BY
        target
)
SELECT 
    superfamily_count,
    COUNT(*) AS target_count
FROM 
    superfamily_counts
GROUP BY
    superfamily_count
ORDER BY
    superfamily_count;
    
