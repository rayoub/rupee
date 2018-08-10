
CREATE OR REPLACE FUNCTION get_ecod_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.ecod_id AS db_id,
        g.grams
    FROM 
        ecod_grams g
        INNER JOIN ecod_domain d
            ON d.ecod_id = g.ecod_id
    WHERE 
        d.ecod_sid % p_split_count = p_split_index 
    ORDER BY 
        d.ecod_sid;

END;
$$LANGUAGE plpgsql;


