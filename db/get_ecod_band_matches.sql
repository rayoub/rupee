
CREATE OR REPLACE FUNCTION get_ecod_band_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_band_index INTEGER, 
    p_different_h BOOLEAN, 
    p_different_t BOOLEAN,
    p_different_f BOOLEAN
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
    DECLARE band_value INTEGER;
    DECLARE q_x VARCHAR;
    DECLARE q_h INTEGER;
    DECLARE q_t INTEGER;
    DECLARE q_f INTEGER;
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
                h.scop_id = p_db_id;

        ELSIF p_search_type = 2 THEN
            
            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                cath_hashes h
            WHERE
                h.cath_id = p_db_id;
        ELSIF p_search_type = 3 THEN
        
            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                ecod_hashes h
            WHERE
                h.ecod_id = p_db_id;

        ELSIF p_search_type = 4 THEN

            SELECT
                h.band_hashes[p_band_index]
            INTO
                band_value
            FROM
                chain_hashes h
            WHERE
                h.chain_id = p_db_id;

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

    IF (p_different_h OR p_different_t OR p_different_f) AND p_upload_id = -1 THEN

        -- get SCOP value of query  
        SELECT 
            d.x, d.h, d.t, d.f
        INTO 
            q_x, q_h, q_t, q_f
        FROM
            ecod_domain d
        WHERE
            d.ecod_id = p_db_id;
        
        RETURN QUERY
        SELECT 
            h.ecod_id AS db_id,
            d.pdb_id AS pdb_id,
            h.min_hashes,
            h.band_hashes
        FROM
            ecod_domain d
            INNER JOIN ecod_hashes h
                ON h.ecod_id = d.ecod_id
        WHERE  
            h.ecod_id = p_db_id
            OR 
            (
                h.band_hashes[p_band_index] = band_value
                AND (p_different_h = FALSE OR d.x <> q_x OR d.h <> q_h)
                AND (p_different_t = FALSE OR d.x <> q_x OR d.h <> q_h OR d.t <> q_t)
                AND (p_different_f = FALSE OR d.x <> q_x OR d.h <> q_h OR d.t <> q_t OR d.f <> q_f)
            );

    ELSE

        RETURN QUERY
        SELECT 
            h.ecod_id AS db_id,
            d.pdb_id AS pdb_id,
            h.min_hashes,
            h.band_hashes
        FROM
            ecod_domain d
            INNER JOIN ecod_hashes h
                ON h.ecod_id = d.ecod_id
        WHERE  
            h.ecod_id = p_db_id
            OR h.band_hashes[p_band_index] = band_value;

    END IF;

END;
$$LANGUAGE plpgsql;


