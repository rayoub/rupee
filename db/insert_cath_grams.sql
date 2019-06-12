
CREATE OR REPLACE FUNCTION insert_cath_grams(p_db_id VARCHAR, p_grams INTEGER ARRAY, p_coords REAL ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO cath_grams (cath_id, grams, coords)
    VALUES (p_db_id, p_grams, p_coords);

END;
$$ LANGUAGE plpgsql;
