
CREATE OR REPLACE FUNCTION get_scop_grams (p_scop_ids VARCHAR ARRAY, p_include_coords INTEGER)
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
        CASE WHEN p_include_coords = 1 THEN g.coords ELSE NULL END AS coords
    FROM
        scop_grams g
        INNER JOIN UNNEST(p_scop_ids) AS ids (scop_id)
            ON ids.scop_id = g.scop_id;

END;
$$LANGUAGE plpgsql;


