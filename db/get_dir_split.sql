
CREATE OR REPLACE FUNCTION get_dir_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        d.db_id,
        d.db_id AS pdb_id
    FROM 
        dir_chain d 
    WHERE 
        d.db_sid % p_split_count = p_split_index 
    ORDER BY 
        d.db_sid;

END;
$$LANGUAGE plpgsql;


