
CREATE OR REPLACE FUNCTION get_cath_split_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_split_index INTEGER, 
    p_split_count INTEGER,
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
    sort_key VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
    DECLARE q_c INTEGER;
    DECLARE q_a INTEGER;
    DECLARE q_t INTEGER;
    DECLARE q_h INTEGER;
    DECLARE q_s INTEGER;
BEGIN

    -- mutual exclusion of reps and differences will be enforced in UI
    
    IF p_topology_reps OR p_superfamily_reps OR p_s35_reps THEN

        RETURN QUERY
        SELECT 
            d.cath_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams,
            g.coords
        FROM
            cath_domain d
            INNER JOIN cath_grams g
                ON g.cath_id = d.cath_id
        WHERE  
            d.cath_sid % p_split_count = p_split_index
            AND 
            (
                d.cath_id = p_db_id
                OR
                (
                    (p_topology_reps = FALSE OR d.topology_rep = TRUE)
                    AND (p_superfamily_reps = FALSE OR d.superfamily_rep = TRUE)
                    AND (p_s35_reps = FALSE OR d.s35_rep = TRUE)
                )
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
            d.cath_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams,
            g.coords
        FROM
            cath_domain d
            INNER JOIN cath_grams g
                ON g.cath_id = d.cath_id
        WHERE  
            d.cath_sid % p_split_count = p_split_index
            AND
            ( 
                d.cath_id = p_db_id
                OR
                (
                    (p_different_topology = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t)
                    AND (p_different_superfamily = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t OR d.h <> q_h)
                    AND (p_different_s35 = FALSE OR d.c <> q_c OR d.a <> q_a OR d.t <> q_t OR d.h <> q_h OR d.s <> q_s)
                )
            );

    ELSE

        RETURN QUERY
        SELECT 
            d.cath_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams,
            g.coords
        FROM
            cath_domain d
            INNER JOIN cath_grams g
                ON g.cath_id = d.cath_id
        WHERE  
            d.cath_sid % p_split_count = p_split_index;
              
    END IF;

END;
$$LANGUAGE plpgsql;


