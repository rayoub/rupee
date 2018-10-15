CREATE OR REPLACE FUNCTION insert_scop_sets (p_tab scop_set ARRAY)
RETURNS VOID
AS $$
BEGIN

    DELETE FROM scop_set WHERE similarity = -1.0;

    INSERT INTO scop_set (pivot_db_id, member_db_id, similarity)
    SELECT
        pivot_db_id,
        member_db_id,
        similarity
    FROM
        UNNEST(p_tab);

    --UPDATE scop_hashes
    --SET set_id = NULL;

    --UPDATE scop_hashes
    --SET set_id = scop_set.pivot_db_id
    --FROM scop_set
    --WHERE db_id = scop_set.member_db_id;

    --UPDATE scop_hashes
    --SET set_id = db_id
    --WHERE set_id IS NULL;
                
END;
$$ LANGUAGE plpgsql;
