
CREATE OR REPLACE FUNCTION get_chain_augmented_results(p_chain_ids VARCHAR ARRAY)
RETURNS TABLE (
    chain_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        c.chain_id
    FROM
        chain c
        INNER JOIN UNNEST(p_chain_ids) WITH ORDINALITY AS ids (chain_id, n)
            ON  ids.chain_id = c.chain_id
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


