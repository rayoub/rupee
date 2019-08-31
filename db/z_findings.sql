
WITH rupee_res_m AS
(
    SELECT DISTINCT
        r.db_id_1 AS rm_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS rm_best_match,
        first_value(s.tm_q_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS rm_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_chain_v06_26_2019'
),
rupee_res_c AS
(
    SELECT DISTINCT
        r.db_id_1 AS rc_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rc_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rc_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_cath_v4_2_0'
),
rupee_res_s AS
(
    SELECT DISTINCT
        r.db_id_1 AS rs_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rs_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rs_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_scop_v1_73'
),
mtm_res AS
(
    SELECT DISTINCT
        m.db_id_1 AS m_db_id_1,
        first_value(m.db_id_2) OVER (PARTITION BY m.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS m_best_match,
        first_value(s.tm_q_tm_score) OVER (PARTITION BY m.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS m_best_score
    FROM
        mtm_result m
        INNER JOIN alignment_scores s
            ON m.db_id_1 = s.db_id_1
            AND m.db_id_2 = s.db_id_2
            AND m.version = s.version
    WHERE
        m.version = 'casp_chain_v06_26_2019'
),
cath_res AS
(
    SELECT DISTINCT
        c.db_id_1 AS c_db_id_1,
        first_value(c.db_id_2) OVER (PARTITION BY c.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS c_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY c.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS c_best_score
    FROM
        cathedral_result c
        INNER JOIN alignment_scores s
            ON c.db_id_1 = s.db_id_1
            AND c.db_id_2 = s.db_id_2
            AND c.version = s.version
    WHERE
        c.version = 'casp_cath_v4_2_0'
),
ssm_res AS
(
    SELECT DISTINCT
        r.db_id_1 AS s_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS s_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS s_best_score
    FROM
        ssm_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_scop_v1_73'
),
comps AS
(
    SELECT
        rm.rm_db_id_1 AS db_id_1,
        rm.rm_best_score - m.m_best_score AS rm_diff,
        rc.rc_best_score - c.c_best_score AS rc_diff,
        rs.rs_best_score - s.s_best_score AS rs_diff,
        rm.rm_best_match,
        rm.rm_best_score,
        m.m_best_match,
        m.m_best_score,
        rc.rc_best_match,
        rc.rc_best_score,
        c.c_best_match,
        c.c_best_score,
        rs.rs_best_match,
        rs.rs_best_score,
        s.s_best_match,
        s.s_best_score
    FROM
        rupee_res_m rm
        INNER JOIN rupee_res_c rc
            ON rm.rm_db_id_1 = rc.rc_db_id_1
        INNER JOIN rupee_res_s rs
            ON rm.rm_db_id_1 = rs.rs_db_id_1
        INNER JOIN mtm_res m
            ON rm.rm_db_id_1 = m.m_db_id_1
        INNER JOIN cath_res c
            ON rm.rm_db_id_1 = c.c_db_id_1
        INNER JOIN ssm_res s
            ON rm.rm_db_id_1 = s.s_db_id_1
)
SELECT
    *
FROM
    comps
WHERE
    (
    rm_diff > 0.05
    AND rc_diff > 0.05
    AND rs_diff > 0.05
    )
    OR
    (
    rm_diff > 0.15
    AND (m_best_score > (GREATEST(c_best_score, s_best_score) + 0.20))
    )
/*WHERE
    db_id_1 IN (
        'T0859-D1-BAKER-ROSETTASERVER',
        'T0866-D1-RaptorX',
        'T0918-D1-BAKER-ROSETTASERVER',
        'T0918-D3-MULTICOM-NOVEL'
    )*/
ORDER BY
    db_id_1;
