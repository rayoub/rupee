
CREATE OR REPLACE FUNCTION get_scop_augmented_results(p_scop_ids VARCHAR ARRAY)
RETURNS TABLE (
    scop_id VARCHAR,
    sunid INTEGER,
    cl VARCHAR,
    cf INTEGER,
    sf INTEGER,
    fa INTEGER,
    cf_description VARCHAR,
    sf_description VARCHAR,
    fa_description VARCHAR
)
AS $$
BEGIN

    RETURN QUERY
    SELECT
        d.scop_id,
        d.sunid,
        d.cl,
        d.cf,
        d.sf,
        d.fa,
        cf.description AS cf_description,
        sf.description AS sf_description,
        fa.description AS fa_description
    FROM
        scop_domain d
        INNER JOIN UNNEST(p_scop_ids) WITH ORDINALITY AS ids (scop_id, n)
            ON  ids.scop_id = d.scop_id
        INNER JOIN scop_name cf
            ON cf.scop_name = d.cl_cf
        INNER JOIN scop_name sf
            ON sf.scop_name = d.cl_cf_sf
        INNER JOIN scop_name fa
            On fa.scop_name = d.cl_cf_sf_fa
    ORDER BY
        ids.n;

END;
$$LANGUAGE plpgsql;


