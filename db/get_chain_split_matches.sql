
CREATE OR REPLACE FUNCTION get_chain_split_matches (
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
    grams INTEGER ARRAY
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        c.chain_id AS db_id,
        c.pdb_id,
        c.sort_key,
        g.grams
    FROM
        chain c
        INNER JOIN chain_grams g
            ON g.chain_id = c.chain_id
    WHERE  
            c.chain_sid % p_split_count = p_split_index;

END;
$$LANGUAGE plpgsql;


