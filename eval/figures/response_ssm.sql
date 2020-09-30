
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
        b.name = 'casp_ssm_d248'
        AND rt.version = 'casp_scop_v1_73'
        AND rt.search_type = 'q_score'
),
ssm_response AS
(
    SELECT
        'ssm' AS app,
        ROW_NUMBER() OVER (ORDER BY ct.residue_count, st.db_id) AS n,
        st.db_id,
        st.time,
        ct.residue_count
    FROM
        ssm_time st
        INNER JOIN benchmark b
            ON st.db_id = b.db_id
        INNER JOIN casp_target ct
            ON st.db_id = ct.db_id
    WHERE
        b.name = 'casp_ssm_d248'
)
SELECT 
    app,
    n,
    db_id,
    time,
    residue_count
FROM 
    rupee_response
UNION ALL
SELECT 
    app,
    n,
    db_id,
    time,
    residue_count
FROM 
    ssm_response;


