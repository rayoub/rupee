
CREATE OR REPLACE FUNCTION get_fs_domains ()
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
    WITH domall AS 
    (
        SELECT
            d.scop_id,
            d.pdb_id,
            d.cl,
            d.cf,
            d.sf,
            d.fa,
            ROW_NUMBER() OVER (PARTITION BY d.cl_cf_sf_fa ORDER BY d.scop_id) AS n
        FROM
            scop_domain d
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
    WHERE 
        d.n = 1
    ORDER BY 
        d.cl,
        d.cf,
        d.sf,
        d.fa;

END;
$$LANGUAGE plpgsql;


