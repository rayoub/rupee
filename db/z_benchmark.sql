
WITH valid_id AS
(
    SELECT
        db_id_1 AS db_id
    FROM
        mtm_result
    WHERE
        version = 'casp_chain_v01_01_2020' -- change this
        AND n = 10 -- we need this number of records in 'other' results
),
valid_domain AS
(
    SELECT
        target,
        domain,
        neff / len AS neff
    FROM
        eu_neff
    ORDER BY
        (neff / len)
    LIMIT 5 -- top 5 and top 10 (top means lower neff)
)
SELECT
    'casp_mtm_d97,' || db_id -- change this
FROM
    valid_id id
    INNER JOIN valid_domain d
        ON id.db_id LIKE d.target || '%' || d.domain
ORDER BY
    db_id;


