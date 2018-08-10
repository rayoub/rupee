
CREATE OR REPLACE FUNCTION get_chain_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.chain_id AS db_id,
        g.grams
    FROM 
        chain_grams g
        INNER JOIN chain c
            ON c.chain_id = g.chain_id
    WHERE 
        c.chain_sid % p_split_count = p_split_index 
    ORDER BY 
        c.chain_sid;

END;
$$LANGUAGE plpgsql;


