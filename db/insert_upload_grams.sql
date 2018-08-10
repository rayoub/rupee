
CREATE OR REPLACE FUNCTION insert_upload_grams(p_grams INTEGER ARRAY)
RETURNS INTEGER
AS $$
DECLARE
    id INTEGER := -1;
BEGIN

    INSERT INTO upload_grams (grams)
    VALUES (p_grams)
    RETURNING upload_id INTO id;
    
    RETURN id;

END;
$$ LANGUAGE plpgsql;
