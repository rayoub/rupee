
WITH rupee_res AS
(
    SELECT
        *
    FROM
        rupee_result r
    WHERE
        r.version = 'casp_chain_v06_26_2019'
        AND r.db_id_1 = 'T0862-D1-BAKER-ROSETTASERVER'
),
mtm_res AS
(
    SELECT
        *
    FROM
        mtm_result v
    WHERE
        v.version = 'casp_chain_v06_26_2019'
        AND v.db_id_1 = 'T0862-D1-BAKER-ROSETTASERVER'
),
combined AS
(
    SELECT
        r.n AS r_n,
        r.db_id_1 AS r_db_id_1,
        r.db_id_2 AS r_db_id_2,
        m.n AS m_n,
        m.db_id_1 AS m_db_id_1,
        m.db_id_2 AS m_db_id_2,
        COALESCE(r.db_id_1, m.db_id_1) AS db_id_1,
        COALESCE(r.db_id_2, m.db_id_2) AS db_id_2
    FROM    
        rupee_res r
        FULL OUTER JOIN mtm_res m
            ON m.db_id_1 = r.db_id_1
            AND m.db_id_2 = r.db_id_2
    WHERE
        1 = 1
)
SELECT
    c.r_n,
    c.r_db_id_1,
    c.r_db_id_2,
    c.m_n,
    c.m_db_id_1,
    c.m_db_id_2,
    s.tm_avg_tm_score,
    s.tm_q_tm_score
FROM
    combined c
    INNER JOIN alignment_scores s
        ON s.db_id_1 = c.db_id_1
        AND s.db_id_2 = c.db_id_2
WHERE
    s.version = 'casp_chain_v06_26_2019'
ORDER BY
    s.tm_q_tm_score DESC
;
