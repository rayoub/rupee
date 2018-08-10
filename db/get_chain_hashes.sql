
CREATE OR REPLACE FUNCTION get_chain_hashes (p_chain_id VARCHAR)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.chain_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        chain_hashes h
    WHERE  
        h.chain_id = p_chain_id;

END;
$$LANGUAGE plpgsql;


