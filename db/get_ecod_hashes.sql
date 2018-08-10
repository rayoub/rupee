
CREATE OR REPLACE FUNCTION get_ecod_hashes (p_ecod_id VARCHAR)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.ecod_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        ecod_hashes h
    WHERE  
        h.ecod_id = p_ecod_id;

END;
$$LANGUAGE plpgsql;


