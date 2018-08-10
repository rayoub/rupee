
CREATE OR REPLACE FUNCTION insert_ecod_grams(p_db_id VARCHAR, p_grams INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO ecod_grams (ecod_id, grams)
    VALUES (p_db_id, p_grams);

END;
$$ LANGUAGE plpgsql;
