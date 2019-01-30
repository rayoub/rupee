

SELECT
    d.cl_cf,
    COUNT(*)
FROM
    scop_domain d
    INNER JOIN benchmark b
        ON b.db_id = d.scop_id
        AND b.name = 'scop_d62'
GROUP BY
    d.cl_cf;  

