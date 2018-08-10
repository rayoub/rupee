
CREATE OR REPLACE FUNCTION insert_chain_grams(p_db_id VARCHAR, p_grams INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO chain_grams (chain_id, grams)
    VALUES (p_db_id, p_grams);

END;
$$ LANGUAGE plpgsql;
