
CREATE OR REPLACE FUNCTION get_scop_split_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_split_index INTEGER, 
    p_split_count INTEGER,
    p_different_fold BOOLEAN, 
    p_different_superfamily BOOLEAN,
    p_different_family BOOLEAN
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    sort_key VARCHAR,
    grams INTEGER ARRAY
)
AS $$
    DECLARE q_cl VARCHAR;
    DECLARE q_cf INTEGER;
    DECLARE q_sf INTEGER;
    DECLARE q_fa INTEGER;
BEGIN

    IF (p_different_fold OR p_different_superfamily OR p_different_family) AND p_upload_id = -1 THEN

        -- get SCOP value of query  
        SELECT 
            d.cl, d.cf, d.sf, d.fa
        INTO 
            q_cl, q_cf, q_sf, q_fa
        FROM
            scop_domain d
        WHERE
            d.scop_id = p_db_id;
        
        RETURN QUERY
        SELECT 
            d.scop_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams
        FROM
            scop_domain d
            INNER JOIN scop_grams g
                ON g.scop_id = d.scop_id
        WHERE  
            d.scop_sid % p_split_count = p_split_index
            AND
            (
                d.scop_id = p_db_id
                OR
                (
                    (p_different_fold = FALSE OR d.cl <> q_cl OR d.cf <> q_cf)
                    AND (p_different_superfamily = FALSE OR d.cl <> q_cl OR d.cf <> q_cf OR d.sf <> q_sf)
                    AND (p_different_family = FALSE OR d.cl <> q_cl OR d.cf <> q_cf OR d.sf <> q_sf OR d.fa <> q_fa)
                )
            )
            AND d.cl <> 'l'; -- exclude artifacts

    ELSE

        RETURN QUERY
        SELECT 
            d.scop_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams
        FROM
            scop_domain d
            INNER JOIN scop_grams g
                ON g.scop_id = d.scop_id
        WHERE 
            d.scop_sid % p_split_count = p_split_index
            AND d.cl <> 'l'; -- exclude artifacts

    END IF;

END;
$$LANGUAGE plpgsql;


