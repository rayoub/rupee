
WITH rupee_res AS
(
    SELECT
        *
    FROM
        rupee_result r
    WHERE
        r.version = 'casp_cath_v4_2_0'
        AND r.db_id_1 = 'T0862-D1-BAKER-ROSETTASERVER'
),
vs_res AS
(
    SELECT
        *
    FROM
        cathedral_result v
    WHERE
        v.version = 'casp_cath_v4_2_0'
        AND v.db_id_1 = 'T0862-D1-BAKER-ROSETTASERVER'
),
combined AS
(
    SELECT
        r.n AS r_n,
        r.db_id_1 AS r_db_id_1,
        r.db_id_2 AS r_db_id_2,
        v.n AS v_n,
        v.db_id_1 AS v_db_id_1,
        v.db_id_2 AS v_db_id_2,
        COALESCE(r.db_id_1, v.db_id_1) AS db_id_1,
        COALESCE(r.db_id_2, v.db_id_2) AS db_id_2
    FROM    
        rupee_res r
        FULL OUTER JOIN vs_res v
            ON v.db_id_1 = r.db_id_1
            AND v.db_id_2 = r.db_id_2
    WHERE
        1 = 1
)
SELECT
    c.r_n,
    c.r_db_id_1,
    c.r_db_id_2,
    c.v_n,
    c.v_db_id_1,
    c.v_db_id_2,
    s.tm_avg_tm_score,
    s.tm_q_tm_score
FROM
    combined c
    INNER JOIN alignment_scores s
        ON s.db_id_1 = c.db_id_1
        AND s.db_id_2 = c.db_id_2
WHERE
    s.version = 'casp_cath_v4_2_0'
ORDER BY
    s.tm_avg_tm_score DESC
;
