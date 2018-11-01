
CREATE OR REPLACE FUNCTION get_search_result (
    p_search_id INTEGER
)
RETURNS TABLE (
    search_id INTEGER,
    n INTEGER,
    db_id VARCHAR,
    pdb_id VARCHAR,
    sort_key VARCHAR,
    similarity NUMERIC,
    rmsd NUMERIC,
    tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    SELECT 
        r.search_id,
        r.n,
        r.db_id,
        r.pdb_id,
        r.sort_key,
        r.similarity,
        r.rmsd,
        r.tm_score
    FROM
        search_result r
    WHERE  
        r.search_id = p_search_id
    ORDER BY
        r.n;

END;
$$LANGUAGE plpgsql;


