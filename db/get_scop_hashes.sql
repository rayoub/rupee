
CREATE OR REPLACE FUNCTION get_scop_hashes (p_scop_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.scop_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        scop_hashes h
        INNER JOIN UNNEST(p_scop_ids) AS ids (scop_id)
            ON ids.scop_id = h.scop_id;

END;
$$LANGUAGE plpgsql;


