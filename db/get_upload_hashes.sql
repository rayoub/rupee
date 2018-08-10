
CREATE OR REPLACE FUNCTION get_upload_hashes (p_upload_id INTEGER)
RETURNS TABLE (
    upload_id INTEGER,
    min_hashes INTEGER ARRAY,
    band_hashes INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        h.upload_id,
        h.min_hashes,
        h.band_hashes
    FROM
        upload_hashes h
    WHERE  
        h.upload_id = p_upload_id;

END;
$$LANGUAGE plpgsql;


