
CREATE OR REPLACE FUNCTION get_cath_hashes (p_cath_id VARCHAR)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.cath_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        cath_hashes h
    WHERE  
        h.cath_id = p_cath_id;

END;
$$LANGUAGE plpgsql;


