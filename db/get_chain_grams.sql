
CREATE OR REPLACE FUNCTION get_chain_grams (p_chain_ids VARCHAR ARRAY, p_include_coords INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.chain_id AS db_id,
        g.grams,
        CASE WHEN p_include_coords = 1 THEN g.coords ELSE NULL END AS coords
    FROM
        chain_grams g
        INNER JOIN UNNEST(p_chain_ids) AS ids (chain_id)
            ON ids.chain_id = g.chain_id;

END;
$$LANGUAGE plpgsql;


