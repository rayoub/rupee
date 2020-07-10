
CREATE OR REPLACE FUNCTION get_dir_split_matches (
    p_search_type INTEGER, 
    p_db_id VARCHAR, 
    p_upload_id INTEGER,
    p_split_index INTEGER, 
    p_split_count INTEGER
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
        d.db_id,
        d.db_id AS pdb_id,
        d.db_id AS sort_key,
        g.grams,
        g.coords
    FROM
        dir_chain d
        INNER JOIN dir_grams g
            ON g.db_id = d.db_id
    WHERE  
            d.db_sid % p_split_count = p_split_index;

END;
$$LANGUAGE plpgsql;


