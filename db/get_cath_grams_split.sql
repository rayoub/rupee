
CREATE OR REPLACE FUNCTION get_cath_grams_split (p_split_index INTEGER, p_split_count INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.cath_id AS db_id,
        g.grams
    FROM 
        cath_grams g
        INNER JOIN cath_domain d
            ON d.cath_id = g.cath_id
    WHERE 
        d.cath_sid % p_split_count = p_split_index 
    ORDER BY 
        d.cath_sid;

END;
$$LANGUAGE plpgsql;


