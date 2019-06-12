
CREATE OR REPLACE FUNCTION insert_upload_grams(p_grams INTEGER ARRAY, p_coords REAL ARRAY)
RETURNS INTEGER
AS $$
DECLARE
    id INTEGER := -1;
BEGIN

    INSERT INTO upload_grams (grams, coords)
    VALUES (p_grams, p_coords)
    RETURNING upload_id INTO id;
    
    RETURN id;

END;
$$ LANGUAGE plpgsql;
