
CREATE OR REPLACE FUNCTION get_afdb_split_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_split_index INTEGER, 
    p_split_count INTEGER,
    p_proteome_id VARCHAR DEFAULT NULL
)
RETURNS TABLE (
    db_id VARCHAR,
    pdb_id VARCHAR,
    sort_key VARCHAR,
    grams INTEGER ARRAY,
    coords REAL ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        p.afdb_id AS db_id,
        p.afdb_id AS pdb_id,
        p.afdb_id AS sort_key,
        g.grams,
        g.coords
    FROM
        afdb_protein p
        INNER JOIN afdb_grams g
            ON g.afdb_id = p.afdb_id
    WHERE  
        p.afdb_sid % p_split_count = p_split_index;

END;
$$LANGUAGE plpgsql;


