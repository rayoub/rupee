
CREATE OR REPLACE FUNCTION insert_chain_hashes(p_chain_id VARCHAR,  p_min_hashes INTEGER ARRAY, p_band_hashes INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

	INSERT INTO chain_hashes (chain_id, min_hashes, band_hashes)
	VALUES (p_chain_id, p_min_hashes, p_band_hashes);

END;
$$ LANGUAGE plpgsql;
