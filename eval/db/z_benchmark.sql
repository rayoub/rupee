/*
SELECT
    r.version,
    r.db_id_1,
    MAX(r.n) AS max_n
FROM
    mtm_result m
GROUP BY
    r.version,
    r.db_id_1
HAVING 
    MAX(r.n) >= 10
ORDER BY
    r.version,
    r.db_id_1;
*/

SELECT
    'casp_ssm_d248,' || r.db_id_1
FROM
    ssm_result r
GROUP BY
    r.version,
    r.db_id_1
HAVING 
    MAX(r.n) >= 100
ORDER BY
    r.version,
    r.db_id_1;

