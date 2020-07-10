
CREATE OR REPLACE FUNCTION get_dir_augmented_results(p_db_ids VARCHAR ARRAY)
RETURNS TABLE (
    db_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        d.db_id
    FROM
        dir_chain d
        INNER JOIN UNNEST(p_db_ids) WITH ORDINALITY AS ids (db_id, n)
            ON  ids.db_id = d.db_id
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


