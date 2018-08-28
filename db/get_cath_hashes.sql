
CREATE OR REPLACE FUNCTION get_cath_hashes (p_db_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY,
    exact_hash BIGINT
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.db_id,
        h.min_hashes,
        h.band_hashes,
        h.exact_hash
    FROM
        cath_hashes h
        INNER JOIN UNNEST(p_db_ids) AS ids (db_id)
            ON ids.db_id = h.db_id;

END;
$$LANGUAGE plpgsql;


