
CREATE OR REPLACE FUNCTION get_fs_sims (p_search_type VARCHAR, p_across_type VARCHAR)
RETURNS TABLE (
    n BIGINT, 

    scop_id_1 VARCHAR,
    sunid_1 INTEGER,
    cl_cf_1 VARCHAR,
    cf_descr_1 VARCHAR,
    cl_cf_sf_1 VARCHAR,
    sf_descr_1 VARCHAR,

    scop_id_2 VARCHAR,
    sunid_2 INTEGER,
    cl_cf_2 VARCHAR,
    cf_descr_2 VARCHAR,
    cl_cf_sf_2 VARCHAR,
    sf_descr_2 VARCHAR, 
    tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH domall AS 
    (
        SELECT
            s.scop_id_1,
            d1.sunid AS sunid_1,
            d1.cl_cf AS cl_cf_1,
            ncf1.description AS cf_descr_1,
            d1.cl_cf_sf AS cl_cf_sf_1,
            nsf1.description AS sf_descr_1,
            
            s.scop_id_2,
            d2.sunid AS sunid_2,
            d2.cl_cf AS cl_cf_2,
            ncf2.description AS cf_descr_2,
            d2.cl_cf_sf AS cl_cf_sf_2,
            nsf2.description AS sf_descr_2,

            s.tm_score
        FROM
            fs_sims s
            INNER JOIN scop_grams g1
                ON g1.scop_id = s.scop_id_1
            INNER JOIN scop_grams g2
                ON g2.scop_id = s.scop_id_2
            INNER JOIN scop_domain d1
                ON d1.scop_id = s.scop_id_1
            INNER JOIN scop_domain d2
                ON d2.scop_id = s.scop_id_2
            INNER JOIN scop_name ncf1
                ON ncf1.scop_name = d1.cl_cf
            INNER JOIN scop_name ncf2
                ON ncf2.scop_name = d2.cl_cf
            INNER JOIN scop_name nsf1
                ON nsf1.scop_name = d1.cl_cf_sf
            INNER JOIN scop_name nsf2
                ON nsf2.scop_name = d2.cl_cf_sf
        WHERE
            s.search_type = p_search_type
            AND s.across_type = p_across_type
    )
    SELECT 
        ROW_NUMBER() OVER (ORDER BY d.tm_score DESC, d.scop_id_1) AS n,

        d.scop_id_1,
        d.sunid_1,
        d.cl_cf_1,
        d.cf_descr_1,
        d.cl_cf_sf_1,
        d.sf_descr_1,

        d.scop_id_2,
        d.sunid_2,
        d.cl_cf_2,
        d.cf_descr_2,
        d.cl_cf_sf_2,
        d.sf_descr_2,
        
        d.tm_score
    FROM 
        domall d
    WHERE
        1 = 1
    ORDER BY
        d.tm_score DESC,
        d.scop_id_1;

END;
$$LANGUAGE plpgsql;


