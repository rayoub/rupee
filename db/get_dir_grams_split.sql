
CREATE OR REPLACE FUNCTION get_dir_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.db_id,
        g.grams
    FROM 
        dir_grams g
        INNER JOIN dir_chain d
            ON d.db_id = g.db_id
    WHERE 
        d.db_sid % p_split_count = p_split_index 
    ORDER BY 
        d.db_sid;

END;
$$LANGUAGE plpgsql;


