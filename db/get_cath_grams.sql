
CREATE OR REPLACE FUNCTION get_cath_grams (p_cath_ids VARCHAR ARRAY, p_include_coords INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.cath_id AS db_id,
        g.grams,
        CASE WHEN p_include_coords = 1 THEN g.coords ELSE NULL END AS coords
    FROM
        cath_grams g
        INNER JOIN UNNEST(p_cath_ids) AS ids (cath_id)
            ON ids.cath_id = g.cath_id;

END;
$$LANGUAGE plpgsql;


