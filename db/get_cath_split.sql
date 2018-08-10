
CREATE OR REPLACE FUNCTION get_cath_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        d.cath_id AS db_id,
        d.pdb_id
    FROM 
        cath_domain d 
    WHERE 
        d.cath_sid % p_split_count = p_split_index 
    ORDER BY 
        d.cath_sid;

END;
$$LANGUAGE plpgsql;


