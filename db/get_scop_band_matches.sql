
CREATE OR REPLACE FUNCTION get_scop_band_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_band_index INTEGER, 
    p_different_fold BOOLEAN, 
    p_different_superfamily BOOLEAN,
    p_different_family BOOLEAN
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    sort_key VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
    DECLARE band_value INTEGER;
    DECLARE q_cl VARCHAR;
    DECLARE q_cf INTEGER;
    DECLARE q_sf INTEGER;
    DECLARE q_fa INTEGER;
BEGIN

    -- get band value of query
    IF p_upload_id = -1 THEN

        IF p_search_type = 1 THEN 

            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                scop_hashes h
            WHERE
                h.db_id = p_db_id;

        ELSIF p_search_type = 2 THEN
            
            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                cath_hashes h
            WHERE
                h.db_id = p_db_id;
        
        ELSIF p_search_type = 3 THEN

            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                ecod_hashes h
            WHERE
                h.db_id = p_db_id;

        ELSIF p_search_type = 4 THEN

            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                chain_hashes h
            WHERE
                h.db_id = p_db_id;

        ELSIF p_search_type = 5 THEN

            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                afdb_hashes h
            WHERE
                h.db_id = p_db_id;
        
        END IF;

    ELSE -- UPLOAD

        SELECT
            h.band_hashes[p_band_index]
        INTO
            band_value
        FROM
            upload_hashes h
        WHERE
            h.upload_id = p_upload_id;

    END IF;

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
            h.db_id,
            d.pdb_id,
            d.sort_key,
            h.min_hashes,
            h.band_hashes
        FROM
            scop_domain d
            INNER JOIN scop_hashes h
                ON h.db_id = d.scop_id
        WHERE
            1 = 1
            -- AND d.cl IN ('c') -- uncomment for find similar
            AND
            (
                h.db_id = p_db_id
                OR 
                (
                    h.band_hashes[p_band_index] = band_value
                    AND (p_different_fold = FALSE OR d.cl <> q_cl OR d.cf <> q_cf)
                    AND (p_different_superfamily = FALSE OR d.cl <> q_cl OR d.cf <> q_cf OR d.sf <> q_sf)
                    AND (p_different_family = FALSE OR d.cl <> q_cl OR d.cf <> q_cf OR d.sf <> q_sf OR d.fa <> q_fa)
                )
            );

    ELSE

        RETURN QUERY
        SELECT 
            h.db_id,
            d.pdb_id,
            d.sort_key,
            h.min_hashes,
            h.band_hashes
        FROM
            scop_domain d
            INNER JOIN scop_hashes h
                ON h.db_id = d.scop_id
        WHERE  
            1 = 1
            -- AND d.cl IN ('c') -- uncomment for find similar
            AND
            (
                h.db_id = p_db_id
                OR h.band_hashes[p_band_index] = band_value
            );

    END IF;

END;
$$LANGUAGE plpgsql;


