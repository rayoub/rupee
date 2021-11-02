
CREATE OR REPLACE FUNCTION get_afdb_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.afdb_id AS db_id,
        g.grams
    FROM 
        afdb_grams g
        INNER JOIN afdb_protein p
            ON p.afdb_id = g.afdb_id
    WHERE 
        p.afdb_sid % p_split_count = p_split_index 
    ORDER BY 
        p.afdb_sid;

END;
$$LANGUAGE plpgsql;


