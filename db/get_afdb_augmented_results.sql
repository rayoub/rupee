
CREATE OR REPLACE FUNCTION get_afdb_augmented_results(p_afdb_ids VARCHAR ARRAY)
RETURNS TABLE (
    proteome_id VARCHAR,
    species VARCHAR,
    common_name VARCHAR,
    afdb_id VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        o.proteome_id,
        o.species,
        o.common_name,
        p.afdb_id
    FROM
        afdb_proteome o
        INNER JOIN afdb_protein p
            ON p.proteome_id = o.proteome_id
        INNER JOIN UNNEST(p_afdb_ids) WITH ORDINALITY AS ids (afdb_id, n)
            ON ids.afdb_id = p.afdb_id
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


