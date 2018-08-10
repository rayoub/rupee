
CREATE OR REPLACE FUNCTION get_scop_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        d.scop_id AS db_id,
        d.pdb_id
    FROM 
        scop_domain d 
    WHERE 
        d.scop_sid % p_split_count = p_split_index 
    ORDER BY 
        d.scop_sid;

END;
$$LANGUAGE plpgsql;


