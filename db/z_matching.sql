
-- this also verifies that our case-sensiivity/insensitivity is where it should be
-- in particular, chain ids are case-sensitive except for scop domains
-- everything else is normalized to lowercase

-- ssm non-matches
--SELECT  
--    r.*
--FROM
--    ssm_result r
--    LEFT JOIN scop_grams g
--        ON g.scop_id = r.db_id_2
--WHERE
--    1 = 1
--    AND g.scop_id IS NULL
--    AND r.n <= 100
--;

-- cathedral non-matches
--SELECT  
--    r.*
--FROM
--    cathedral_result r
--    LEFT JOIN cath_grams g
--        ON g.cath_id = r.db_id_2
--WHERE
--    1 = 1
--    AND g.cath_id IS NULL
--    AND r.n <= 100
--;

-- since mtm returns non-matches because we do not parse split pdbs
-- we need to take their value as is corresponding to tm_q_tm_score
-- it is always the same as our own since they both use tm_align
-- so that makes it simple and trust worthy and also doesn't bias
-- against mtm because they have better parsing
-- this needs to be taken care of in the get_mtm_results function

-- mtm non-matches
SELECT  
    r.*
FROM
    mtm_result r
    LEFT JOIN chain_grams g
        ON g.chain_id = r.db_id_2
WHERE
    1 = 1
    AND g.chain_id IS NULL
    AND r.n <= 100
;
    
