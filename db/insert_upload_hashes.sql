
CREATE OR REPLACE FUNCTION insert_upload_hashes(p_upload_id INTEGER, p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO upload_hashes (upload_id, min_hashes, band_hashes)
	VALUES (p_upload_id, p_min_hashes, p_band_hashes);

END;
$$ LANGUAGE plpgsql;
