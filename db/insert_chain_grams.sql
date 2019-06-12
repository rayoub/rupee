
CREATE OR REPLACE FUNCTION insert_chain_grams(p_db_id VARCHAR, p_grams INTEGER ARRAY, p_coords REAL ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO chain_grams (chain_id, grams, coords)
    VALUES (p_db_id, p_grams, p_coords);

END;
$$ LANGUAGE plpgsql;
