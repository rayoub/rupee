
WITH domains AS
(
    SELECT
        'casp_chain_v06_26_2019' As version,
        b.db_id
    FROM
        benchmark b
    WHERE
        b.name = 'casp_mtm_d144'
),
rupee_res AS
(
    SELECT
        r.n,
        r.db_id_1,
        r.db_id_2,
        s.tm_q_tm_score AS q_score,
        s.tm_avg_tm_score AS avg_score
    FROM
        domains d
        INNER JOIN rupee_result r
            ON r.db_id_1 = d.db_id
            AND r.version = d.version
        INNER JOIN alignment_scores s
            ON s.db_id_1 = r.db_id_1
            AND s.db_id_2 = r.db_id_2
),
mtm_res AS
(
    SELECT
        r.n,
        r.db_id_1,
        r.db_id_2,
        s.tm_q_tm_score AS q_score,
        s.tm_avg_tm_score AS avg_score
    FROM
        domains d
        INNER JOIN mtm_result_matched r
            ON r.db_id_1 = d.db_id
            AND r.version = d.version
        INNER JOIN alignment_scores s
            ON s.db_id_1 = r.db_id_1
            AND s.db_id_2 = r.db_id_2
)
SELECT
    r.*
FROM
    rupee_res r
    LEFT JOIN mtm_res m
        ON r.db_id_1 = m.db_id_1
        AND r.db_id_2 = m.db_id_2
WHERE
    r.n <= 10
    AND m.n IS NULL
ORDER BY
    r.avg_score DESC;
    
