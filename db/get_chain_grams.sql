
CREATE OR REPLACE FUNCTION get_chain_grams (p_chain_ids VARCHAR ARRAY)
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
        g.coords
    FROM
        chain_grams g
        INNER JOIN UNNEST(p_chain_ids) AS ids (chain_id)
            ON ids.chain_id = g.chain_id;

END;
$$LANGUAGE plpgsql;


