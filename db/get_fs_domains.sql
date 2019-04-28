
CREATE OR REPLACE FUNCTION get_fs_domains ()
RETURNS TABLE (
    scop_id VARCHAR,
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
            d.cl,
            d.cf,
            d.sf,
            d.fa
        FROM
            astral a
            INNER JOIN scop_domain d
                ON d.scop_id = a.scop_id
            INNER JOIN scop_grams g
                ON g.scop_id = d.scop_id
        WHERE
            d.cl IN ('c')
            AND CARDINALITY(g.grams) BETWEEN 50 AND 500
    )
    SELECT 
        d.scop_id,
        d.cl,
        d.cf,
        d.sf,
        d.fa
    FROM 
        domall d 
    ORDER BY 
        d.scop_id,
        d.cl,
        d.cf,
        d.sf,
        d.fa;

END;
$$LANGUAGE plpgsql;


