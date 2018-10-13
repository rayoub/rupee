
CREATE OR REPLACE FUNCTION insert_scop_pairs(p_tab scop_pair ARRAY)
RETURNS VOID
AS $$
BEGIN

    INSERT INTO scop_pair(db_id_1, db_id_2, similarity)
    SELECT
        db_id_1,
        db_id_2,
        similarity
    FROM
        UNNEST(p_tab);

END;
$$ LANGUAGE plpgsql;
