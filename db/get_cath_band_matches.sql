
CREATE OR REPLACE FUNCTION get_cath_band_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_band_index INTEGER, 
    p_topology_reps BOOLEAN,
    p_superfamily_reps BOOLEAN,
    p_s35_reps BOOLEAN, 
    p_different_topology BOOLEAN, 
    p_different_superfamily BOOLEAN,
    p_different_s35 BOOLEAN
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
    DECLARE band_value INTEGER;
    DECLARE q_c INTEGER;
    DECLARE q_a INTEGER;
    DECLARE q_t INTEGER;
    DECLARE q_h INTEGER;
    DECLARE q_s INTEGER;
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

    -- mutual exclusion of reps and differences will be enforced in UI
    
    IF p_topology_reps OR p_superfamily_reps OR p_s35_reps THEN

        RETURN QUERY
        SELECT 
            h.cath_id AS db_id,
            d.pdb_id AS pdb_id,
            h.min_hashes,
            h.band_hashes
        FROM
            cath_domain d
            INNER JOIN cath_hashes h
                ON h.cath_id = d.cath_id
        WHERE  
            h.cath_id = p_db_id
            OR
            (
                h.band_hashes[p_band_index] = band_value
                AND (p_topology_reps = FALSE OR d.topology_rep = TRUE)
                AND (p_superfamily_reps = FALSE OR d.superfamily_rep = TRUE )
                AND (p_s35_reps = FALSE OR d.s35_rep = TRUE)
            );

    ELSIF (p_different_topology OR p_different_superfamily OR p_different_s35) AND p_upload_id = -1  THEN

        -- get CATH value of query  
        SELECT 
            d.c, d.a, d.t, d.h, d.s
        INTO 
            q_c, q_a, q_t, q_h, q_s
        FROM
            cath_domain d
        WHERE
            d.cath_id = p_db_id;

        RETURN QUERY
        SELECT 
            h.cath_id AS db_id,
            d.pdb_id AS pdb_id,
            h.min_hashes,
            h.band_hashes
        FROM
            cath_domain d
            INNER JOIN cath_hashes h
                ON h.cath_id = d.cath_id
        WHERE  
            h.cath_id = p_db_id
            OR
            (
                h.band_hashes[p_band_index] = band_value
                AND (p_different_topology = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t)
                AND (p_different_superfamily = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t OR d.h <> q_h)
                AND (p_different_s35 = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t OR d.h <> q_h OR d.s <> q_s)
            );

    ELSE

        RETURN QUERY
        SELECT 
            h.cath_id AS db_id,
            d.pdb_id AS pdb_id,
            h.min_hashes,
            h.band_hashes
        FROM
            cath_domain d
            INNER JOIN cath_hashes h
                ON h.cath_id = d.cath_id
        WHERE  
            h.cath_id = p_db_id
            OR h.band_hashes[p_band_index] = band_value;
              
    END IF;

END;
$$LANGUAGE plpgsql;


