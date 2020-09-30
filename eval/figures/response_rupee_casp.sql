
WITH rupee_response AS
(
    SELECT
        rt.search_mode AS app,
        ROW_NUMBER() OVER (PARTITION BY rt.search_mode ORDER BY ct.residue_count, rt.db_id) AS n,
        rt.db_id,
        rt.time,
        ct.residue_count
    FROM
        rupee_time rt
        INNER JOIN benchmark b
            ON rt.db_id = b.db_id
        INNER JOIN casp_target ct
            ON rt.db_id = ct.db_id
    WHERE
        b.name = 'casp_d250'
        AND rt.version = 'casp_scop_v2_07'
        AND rt.search_type = 'full_length'
)
SELECT 
    app,
    n,
    db_id,
    time,
    residue_count
FROM 
    rupee_response
ORDER BY
    app,
    n;

