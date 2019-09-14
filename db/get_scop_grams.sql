
CREATE OR REPLACE FUNCTION get_scop_grams (p_scop_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.scop_id AS db_id,
        g.grams,
        g.coords
    FROM
        scop_grams g
        INNER JOIN UNNEST(p_scop_ids) AS ids (scop_id)
            ON ids.scop_id = g.scop_id;

END;
$$LANGUAGE plpgsql;


