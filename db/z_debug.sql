
WITH rupee AS
(
    SELECT
        *
    FROM
        rupee_result r
    WHERE
        r.version = 'casp_cath_v4_2_0'
        AND search_mode = 'top_aligned'
        AND search_type = 'ssap_score'
),
scores AS
(
    SELECT
        r.db_id_1,
        r.db_id_2,
    --    r.cathedral_ssap_score,
        s.ssap_score
    FROM
        rupee r
        INNER JOIN alignment_scores s
            ON s.version = r.version
            AND s.db_id_1 = r.db_id_1
            AND s.db_id_2 = r.db_id_2
    ORDER BY
        r.db_id_1,
        r.db_id_2
)
SELECT
    *
FROM
    scores 
WHERE
    ssap_score <= 0;

