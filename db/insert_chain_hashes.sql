
CREATE OR REPLACE FUNCTION insert_chain_hashes(p_db_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO chain_hashes (db_id, min_hashes, band_hashes)
	VALUES (p_db_id, p_min_hashes, p_band_hashes, p_exact_hash);

END;
$$ LANGUAGE plpgsql;
