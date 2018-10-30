
CREATE OR REPLACE FUNCTION insert_search_result(p_tab search_result ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO search_result(search_hash, n, db_id, pdb_id, sort_key, similarity, rmsd, tm_score) 
    SELECT
        search_hash,
        n,
        db_id,
        pdb_id,
        sort_key,
        similarity,
        rmsd,
        tm_score
    FROM
        UNNEST(p_tab);

END;
$$ LANGUAGE plpgsql;
