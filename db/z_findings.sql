
-- this only looks at the top 1 for each query

-- TODO: we need to do this for other scores I will have to annotates or create new ctes per type of comparison

WITH 
rupee_vs_mtm AS
(
    SELECT DISTINCT
        r.db_id_1 AS rm_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS rm_best_match,
        first_value(s.tm_q_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC)::REAL AS rm_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
            AND r.search_type = 'full_length' -- less check only full length matches to be fair to the others
    WHERE
        r.version = 'casp_chain_v01_01_2020'
),
rupee_vs_ssm AS
(
    SELECT DISTINCT
        r.db_id_1 AS rs_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rs_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS rs_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
            AND r.search_type = 'full_length' 
    WHERE
        r.version = 'casp_scop_v1_73'
),
rupee_vs_cathedral AS
(
    SELECT DISTINCT
        r.db_id_1 AS rc_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rc_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS rc_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
            AND r.search_type = 'full_length'
    WHERE
        r.version = 'casp_cath_v4_2_0'
),
rupee_vs_vast AS
(
    SELECT DISTINCT
        r.db_id_1 AS rv_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS rv_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS rv_best_score
    FROM
        rupee_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
            AND r.search_type = 'full_length' 
    WHERE
        r.version = 'casp_chain_v01_01_2020'
),
vs_mtm AS
(
    SELECT DISTINCT
        r.db_id_1 AS m_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC) AS m_best_match,
        first_value(s.tm_q_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_q_tm_score DESC)::REAL AS m_best_score
    FROM
        mtm_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_chain_v01_01_2020'
),
vs_ssm AS
(
    SELECT DISTINCT
        r.db_id_1 AS s_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS s_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS s_best_score
    FROM
        ssm_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_scop_v1_73'
),
vs_cathedral AS
(
    SELECT DISTINCT
        r.db_id_1 AS c_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS c_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS c_best_score
    FROM
        cathedral_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_cath_v4_2_0'
),
vs_vast AS
(
    SELECT DISTINCT
        r.db_id_1 AS v_db_id_1,
        first_value(r.db_id_2) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC) AS v_best_match,
        first_value(s.tm_avg_tm_score) OVER (PARTITION BY r.db_id_1 ORDER BY s.tm_avg_tm_score DESC)::REAL AS v_best_score
    FROM
        vast_result r
        INNER JOIN alignment_scores s
            ON r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
            AND r.version = s.version
    WHERE
        r.version = 'casp_chain_v01_01_2020'
),
comps AS
(
    SELECT
        rm.rm_db_id_1 AS db_id_1,
        (rm.rm_best_score - m.m_best_score)::REAL AS rm_diff,
        (rs.rs_best_score - s.s_best_score)::REAL AS rs_diff,
        (rc.rc_best_score - c.c_best_score)::REAL AS rc_diff,
        (rv.rv_best_score - v.v_best_score)::REAL AS rv_diff,
        rm.rm_best_match,
        rm.rm_best_score,
        m.m_best_match,
        m.m_best_score,
        rs.rs_best_match,
        rs.rs_best_score,
        s.s_best_match,
        s.s_best_score,
        rc.rc_best_match,
        rc.rc_best_score,
        c.c_best_match,
        c.c_best_score,
        rv.rv_best_match,
        rv.rv_best_score,
        v.v_best_match,
        v.v_best_score,
        GREATEST(rs.rs_best_score, rc.rc_best_score, rv.rv_best_score) AS r_best_score,
        GREATEST(m.m_best_score, s.s_best_score, c.c_best_score, v.v_best_score) AS vs_best_score
        --GREATEST(rm.rm_best_score, rs.rs_best_score, rc.rc_best_score, rv.rv_best_score) AS r_best_score,
        --GREATEST(m.m_best_score, s.s_best_score, c.c_best_score, v.v_best_score) AS vs_best_score
    FROM
        rupee_vs_mtm rm
        INNER JOIN rupee_vs_ssm rs
            ON rm.rm_db_id_1 = rs.rs_db_id_1
        INNER JOIN rupee_vs_cathedral rc
            ON rm.rm_db_id_1 = rc.rc_db_id_1
        INNER JOIN rupee_vs_vast rv
            ON rm.rm_db_id_1 = rv.rv_db_id_1
        INNER JOIN vs_mtm m
            ON rm.rm_db_id_1 = m.m_db_id_1
        INNER JOIN vs_ssm s
            ON rm.rm_db_id_1 = s.s_db_id_1
        INNER JOIN vs_cathedral c
            ON rm.rm_db_id_1 = c.c_db_id_1
        INNER JOIN vs_vast v
            ON rm.rm_db_id_1 = v.v_db_id_1
),
filtered_comps AS
(
    SELECT
        db_id_1,
        rm_diff,
        rs_diff,
        rc_diff,
        rv_diff,
        rm_best_match,
        rm_best_score,
        m_best_match,
        m_best_score,
        rs_best_match,
        rs_best_score,
        s_best_match,
        s_best_score,
        rc_best_match,
        rc_best_score,
        c_best_match,
        c_best_score,
        rv_best_match,
        rv_best_score,
        v_best_match,
        v_best_score,
        r_best_score,
        vs_best_score,
        CASE
        WHEN r_best_score = rm_best_score THEN rm_best_match 
        WHEN r_best_score = rs_best_score THEN rs_best_match 
        WHEN r_best_score = rc_best_score THEN rc_best_match 
        WHEN r_best_score = rv_best_score THEN rv_best_match 
        END AS r_best_match
    FROM
        comps
    WHERE
        1 = 1
        AND
        (rm_diff > 0.02 AND rs_diff > 0.02 AND rc_diff > 0.02 AND rv_diff > 0.02)
    --    (
    --    (rs_diff > 0.06 AND rc_diff > 0.06 AND rv_diff > 0.06)
    --    OR
    --    (rm_diff > 0.06 AND rc_diff > 0.06 AND rv_diff > 0.06)
    --    OR
    --    (rm_diff > 0.06 AND rs_diff > 0.06 AND rv_diff > 0.06)
    --    OR
    --    (rm_diff > 0.06 AND rs_diff > 0.06 AND rc_diff > 0.06)
    --    )
)
SELECT
    db_id_1,
    LEAST(rm_diff, rs_diff, rc_diff, rv_diff) AS least_diff,
    r_best_score,
    r_best_match
FROM
    filtered_comps
ORDER BY
    db_id_1;




