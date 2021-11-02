
CREATE OR REPLACE FUNCTION get_afdb_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        p.afdb_id AS db_id,
        p.afdb_id AS pdb_id
    FROM 
        afdb_protein p 
    WHERE 
        p.afdb_sid % p_split_count = p_split_index 
    ORDER BY 
        p.afdb_sid;

END;
$$LANGUAGE plpgsql;


