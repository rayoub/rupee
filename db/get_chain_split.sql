
CREATE OR REPLACE FUNCTION get_chain_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        c.chain_id AS db_id,
        c.pdb_id
    FROM 
        chain c 
    WHERE 
        c.chain_sid % p_split_count = p_split_index 
    ORDER BY 
        c.chain_sid;

END;
$$LANGUAGE plpgsql;


