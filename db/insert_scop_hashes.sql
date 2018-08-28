
CREATE OR REPLACE FUNCTION insert_scop_hashes(p_db_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY, p_exact_hash BIGINT)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO scop_hashes (db_id, min_hashes, band_hashes, exact_hash)
	VALUES (p_db_id, p_min_hashes, p_band_hashes, p_exact_hash);

END;
$$ LANGUAGE plpgsql;
