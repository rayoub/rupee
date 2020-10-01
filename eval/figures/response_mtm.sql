
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
        AND rt.version = 'casp_chain_v08_28_2020'
        AND rt.search_type = 'contained_in'
),
mtm_response AS
(
    SELECT
        'mTM' AS app,
        ROW_NUMBER() OVER (ORDER BY ct.residue_count, mt.db_id) AS n,
        mt.db_id,
        mt.time,
        ct.residue_count
    FROM
        mtm_time mt
        INNER JOIN benchmark b
            ON mt.db_id = b.db_id
        INNER JOIN casp_target ct
            ON mt.db_id = ct.db_id
    WHERE
        b.name = 'casp_d250'
)
SELECT 
    CASE 
        WHEN app = 'all_aligned' THEN 'All'
        WHEN app = 'top_aligned' THEN 'Top'
        WHEN app = 'fast' THEN 'Fast'
    END AS app,
    n,
    db_id,
    (time / 1000)::INTEGER AS time,
    residue_count
FROM 
    rupee_response
UNION ALL
SELECT 
    app,
    n,
    db_id,
    (time / 1000)::INTEGER AS time,
    residue_count
FROM 
    mtm_response;

