
CREATE OR REPLACE FUNCTION get_dir_grams (p_db_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        g.db_id,
        g.grams,
        g.coords
    FROM
        dir_grams g
        INNER JOIN UNNEST(p_db_ids) AS ids (db_id)
            ON ids.db_id = g.db_id;

END;
$$LANGUAGE plpgsql;


