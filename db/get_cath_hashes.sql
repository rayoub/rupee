
CREATE OR REPLACE FUNCTION get_cath_hashes (p_cath_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.cath_id AS db_id,
        h.min_hashes,
        h.band_hashes
    FROM
        cath_hashes h
        INNER JOIN UNNEST(p_cath_ids) AS ids (cath_id)
            ON ids.cath_id = h.cath_id;

END;
$$LANGUAGE plpgsql;


