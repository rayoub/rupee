
CREATE OR REPLACE FUNCTION get_fs_refine (p_search_type VARCHAR, p_across_type VARCHAR)
RETURNS TABLE (
    scop_id VARCHAR,
    pdb_id VARCHAR,
    cl VARCHAR,
    cf INTEGER,
    sf INTEGER,
    fa INTEGER
)
AS $$
BEGIN

    RETURN QUERY
    WITH folds AS
    (
        SELECT 
            d.cl_cf_sf_fa
        FROM
            scop_domain d
            INNER JOIN get_fs_sims(p_search_type, p_across_type) AS s
                ON d.scop_id = s.scop_id_1
        GROUP BY
            d.scop_id,
            d.cl_cf_sf_fa
        ORDER BY
            d.cl_cf_sf_fa
    ),
    domall AS 
    (
        SELECT
            d.scop_id,
            d.pdb_id,
            d.cl,
            d.cf,
            d.sf,
            d.fa
        FROM
            scop_domain d
            INNER JOIN folds f
                ON f.cl_cf_sf_fa = d.cl_cf_sf_fa
            INNER JOIN scop_grams g
                ON g.scop_id = d.scop_id
        WHERE
            d.cl IN ('c')
            AND CARDINALITY(g.grams) BETWEEN 50 AND 500
    )
    SELECT 
        d.scop_id,
        d.pdb_id,
        d.cl,
        d.cf,
        d.sf,
        d.fa
    FROM 
        domall d 
    ORDER BY 
        d.cl,
        d.cf,
        d.sf,
        d.fa;

END;
$$LANGUAGE plpgsql;


