
CREATE OR REPLACE FUNCTION get_benchmark_split (p_name VARCHAR, p_split_count INTEGER, p_split_index INTEGER)
RETURNS TABLE (
    db_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    WITH bm AS
    (
        SELECT
            b.name,
            b.db_id,
            ROW_NUMBER() OVER (ORDER BY b.db_id) AS n
        FROM
            benchmark b
            LEFT JOIN vast_done d
                ON d.db_id = b.db_id
        WHERE
            d.db_id IS NULL
            AND b.name = p_name
        ORDER BY
            b.db_id
    )
    SELECT
        b.db_id
    FROM
        bm b
    WHERE
        b.n % p_split_count = p_split_index
    ORDER BY
        RANDOM();

END;
$$LANGUAGE plpgsql;

