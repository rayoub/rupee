
CREATE OR REPLACE FUNCTION insert_cath_hashes(p_cath_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO cath_hashes (cath_id, min_hashes, band_hashes)
	VALUES (p_cath_id, p_min_hashes, p_band_hashes);

END;
$$ LANGUAGE plpgsql;
