
CREATE OR REPLACE FUNCTION get_chain_hashes (p_chain_ids VARCHAR ARRAY)
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
        INNER JOIN UNNEST(p_chain_ids) AS ids (chain_id)
            ON ids.chain_id = h.chain_id;

END;
$$LANGUAGE plpgsql;


