
CREATE OR REPLACE FUNCTION get_ecod_grams (p_ecod_ids VARCHAR ARRAY, p_include_coords INTEGER)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.ecod_id AS db_id,
        g.grams,
        CASE WHEN p_include_coords = 1 THEN g.coords ELSE NULL END AS coords
    FROM
        ecod_grams g
        INNER JOIN UNNEST(p_ecod_ids) AS ids (ecod_id)
            ON ids.ecod_id = g.ecod_id;

END;
$$LANGUAGE plpgsql;


