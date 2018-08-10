
CREATE OR REPLACE FUNCTION get_scop_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.scop_id AS db_id,
        g.grams
    FROM 
        scop_grams g
        INNER JOIN scop_domain d
            ON d.scop_id = g.scop_id
    WHERE 
        d.scop_sid % p_split_count = p_split_index 
    ORDER BY 
        d.scop_sid;

END;
$$LANGUAGE plpgsql;


