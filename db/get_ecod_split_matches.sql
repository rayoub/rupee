
CREATE OR REPLACE FUNCTION get_ecod_split_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_split_index INTEGER, 
    p_split_count INTEGER,
    p_different_h BOOLEAN, 
    p_different_t BOOLEAN,
    p_different_f BOOLEAN
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    sort_key VARCHAR,
    grams INTEGER ARRAY
)
AS $$
    DECLARE q_x VARCHAR;
    DECLARE q_h VARCHAR;
    DECLARE q_t VARCHAR;
    DECLARE q_f VARCHAR;
BEGIN

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
            d.ecod_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams
        FROM
            ecod_domain d
            INNER JOIN ecod_grams g
                ON g.ecod_id = d.ecod_id
        WHERE  
            d.ecod_sid % p_split_count = p_split_index
            AND
            (
                d.ecod_id = p_db_id
                OR 
                (
                    (p_different_h = FALSE OR d.x <> q_x OR d.h <> q_h)
                    AND (p_different_t = FALSE OR d.x <> q_x OR d.h <> q_h OR d.t <> q_t)
                    AND (p_different_f = FALSE OR d.x <> q_x OR d.h <> q_h OR d.t <> q_t OR d.f <> q_f)
                )
            );

    ELSE

        RETURN QUERY
        SELECT 
            d.ecod_id AS db_id,
            d.pdb_id,
            d.sort_key,
            g.grams
        FROM
            ecod_domain d
            INNER JOIN ecod_grams g
                ON g.ecod_id = d.ecod_id
        WHERE  
            d.ecod_sid % p_split_count = p_split_index;

    END IF;

END;
$$LANGUAGE plpgsql;


