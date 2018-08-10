
CREATE OR REPLACE FUNCTION get_ecod_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        d.ecod_id AS db_id,
        d.pdb_id
    FROM 
        ecod_domain d 
    WHERE 
        d.ecod_sid % p_split_count = p_split_index 
    ORDER BY 
        d.ecod_sid;

END;
$$LANGUAGE plpgsql;


