
CREATE OR REPLACE FUNCTION insert_dir_chains(p_db_ids VARCHAR ARRAY)
RETURNS VOID
AS $$
BEGIN

    TRUNCATE dir_chain;
    TRUNCATE dir_grams;
    TRUNCATE dir_hashes;

	INSERT INTO dir_chain (db_id)
    SELECT u.db_id FROM UNNEST(p_db_ids) AS u(db_id);

END;
$$ LANGUAGE plpgsql;
