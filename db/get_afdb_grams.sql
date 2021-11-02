
CREATE OR REPLACE FUNCTION get_afdb_grams (p_afdb_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.afdb_id AS db_id,
        g.grams,
        g.coords
    FROM
        afdb_grams g
        INNER JOIN UNNEST(p_afdb_ids) AS ids (afdb_id)
            ON ids.afdb_id = g.afdb_id;

END;
$$LANGUAGE plpgsql;


