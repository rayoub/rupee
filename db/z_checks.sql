
-- make sure everything matches (if not work to do)
SELECT
    COUNT(*)
FROM
    ssm_result r
    INNER JOIN scop_domain d
        ON r.db_id_2 = d.scop_id;

-- make sure everything matches (if not work to do)
SELECT 
    COUNT(*) 
FROM
    cathedral_result r
    INNER JOIN cath_domain d
        ON r.db_id_2 = d.cath_id;


