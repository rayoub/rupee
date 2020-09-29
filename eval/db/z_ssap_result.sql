
/*
-- get input to ssap for rupee results
SELECT
    r.db_id_1 || '.pdb',
    r.db_id_2 || '.pdb'
FROM
    rupee_result r
    LEFT JOIN ssap_result s
        ON r.db_id_1 = s.db_id_1 AND r.db_id_2 = s.db_id_2
WHERE
    s.ssap_score IS NULL 
    AND r.version = 'casp_cath_v4_2_0'
    AND r.search_type = 'ssap_score';
*/

-- get input to ssap for cathedral results
SELECT
    r.db_id_1 || '.pdb',
    r.db_id_2 || '.pdb'
FROM
    cathedral_result r
    LEFT JOIN ssap_result s
        ON r.db_id_1 = s.db_id_1 AND r.db_id_2 = s.db_id_2
WHERE
    s.ssap_score IS NULL 
    AND r.version = 'casp_cath_v4_2_0';
