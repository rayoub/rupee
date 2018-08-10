
CREATE OR REPLACE FUNCTION get_scop_hashes (p_scop_id VARCHAR)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.scop_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        scop_hashes h
    WHERE  
        h.scop_id = p_scop_id;

END;
$$LANGUAGE plpgsql;


