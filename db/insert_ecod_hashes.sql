
CREATE OR REPLACE FUNCTION insert_ecod_hashes(p_ecod_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO ecod_hashes (ecod_id, min_hashes, band_hashes)
	VALUES (p_ecod_id, p_min_hashes, p_band_hashes);

END;
$$ LANGUAGE plpgsql;
