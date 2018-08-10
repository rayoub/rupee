
CREATE OR REPLACE FUNCTION insert_scop_grams(p_db_id VARCHAR, p_grams INTEGER ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO scop_grams (scop_id, grams)
    VALUES (p_db_id, p_grams);

END;
$$ LANGUAGE plpgsql;
