
CREATE OR REPLACE FUNCTION get_ecod_augmented_results(p_ecod_ids VARCHAR ARRAY)
RETURNS TABLE (
    ecod_id VARCHAR,
    x VARCHAR,
    h VARCHAR,
    t VARCHAR,
    f VARCHAR,
    architecture VARCHAR,
    x_description VARCHAR,
    h_description VARCHAR,
    t_description VARCHAR,
    f_description VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        d.ecod_id,
        d.x,
        d.h,
        d.t,
        d.f,
        d.architecture,
        d.x_description,
        d.h_description,
        d.t_description,
        d.f_description
    FROM
        ecod_domain d
        INNER JOIN UNNEST(p_ecod_ids) WITH ORDINALITY AS ids (ecod_id, n)
            ON  ids.ecod_id = d.ecod_id
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


