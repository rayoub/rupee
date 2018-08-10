
CREATE OR REPLACE FUNCTION get_cath_augmented_results(p_cath_ids VARCHAR ARRAY)
RETURNS TABLE (
    cath_id VARCHAR,
    c INTEGER,
    a INTEGER,
    t INTEGER,
    h INTEGER,
    s INTEGER,
    o INTEGER,
    l INTEGER,
    i INTEGER,
    d INTEGER,
    a_description VARCHAR,
    t_description VARCHAR,
    h_description VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        d.cath_id,
        d.c, d.a, d.t, d.h,
        d.s, d.o, d.l, d.i, d.d,
        a.description AS a_description,
        t.description AS t_description,
        h.description AS h_description
    FROM
        cath_domain d
        INNER JOIN UNNEST(p_cath_ids) WITH ORDINALITY AS ids (cath_id, n)
            ON  ids.cath_id = d.cath_id
        INNER JOIN cath_name a
            ON a.cath_name = d.ca
        INNER JOIN cath_name t
            ON t.cath_name = d.cat
        INNER JOIN cath_name h
            On h.cath_name = d.cath
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


