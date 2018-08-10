
CREATE OR REPLACE FUNCTION insert_scop_hashes(p_scop_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO scop_hashes (scop_id, min_hashes, band_hashes)
	VALUES (p_scop_id, p_min_hashes, p_band_hashes);

END;
$$ LANGUAGE plpgsql;
