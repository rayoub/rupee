
CREATE OR REPLACE FUNCTION get_ecod_hashes (p_ecod_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.ecod_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        ecod_hashes h
        INNER JOIN UNNEST(p_ecod_ids) AS ids (ecod_id)
            ON ids.ecod_id = h.ecod_id;

END;
$$LANGUAGE plpgsql;


