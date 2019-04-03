
CREATE OR REPLACE FUNCTION get_fs_sims ()
RETURNS TABLE (
    search_type VARCHAR,
    across_type VARCHAR,
    scop_id_1 VARCHAR,
    len_1 INTEGER,
    cl_cf_sf_fa_1 VARCHAR,
    description_1 VARCHAR,
    scop_id_2 VARCHAR,
    len_2 INTEGER,
    cl_cf_sf_fa_2 VARCHAR,
    description_2 VARCHAR,
    rmsd NUMERIC,
    tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH domall AS 
    (
        SELECT
            s.search_type,
            s.across_type,
            s.scop_id_1,
            CARDINALITY(g1.grams) AS len_1,
            d1.cl_cf_sf_fa AS cl_cf_sf_fa_1,
            n1.description AS description_1,
            s.scop_id_2,
            CARDINALITY(g2.grams) AS len_2,
            d2.cl_cf_sf_fa AS cl_cf_sf_fa_2,
            n2.description AS description_2,
            s.rmsd,
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
            INNER JOIN scop_name n1
                ON n1.scop_name = d1.cl_cf_sf_fa
            INNER JOIN scop_name n2
                ON n2.scop_name = d2.cl_cf_sf_fa
    )
    SELECT 
        d.search_type,
        d.across_type,
        d.scop_id_1,
        d.len_1,
        d.cl_cf_sf_fa_1,
        d.description_1,
        d.scop_id_2,
        d.len_2,
        d.cl_cf_sf_fa_2,
        d.description_2,
        d.rmsd,
        d.tm_score
    FROM 
        domall d
    WHERE
        1 = 1
        AND d.description_1 <> 'automated matches'
        AND d.description_2 <> 'automated matches';

END;
$$LANGUAGE plpgsql;


