
SELECT
    r.db_id_1 || '.pdb',
    r.db_id_2 || '.pdb'
FROM
    rupee_result r
    INNER JOIN alignment_scores s
        ON s.db_id_1 = r.db_id_1 AND s.db_id_2 = r.db_id_2
WHERE
    r.version = 'casp_cath_v4_2_0'
    AND r.search_type = 'ssap_score'
    AND s.ssap_score = -1;
     
