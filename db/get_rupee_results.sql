
CREATE OR REPLACE FUNCTION get_rupee_results (p_limit INTEGER)
RETURNS TABLE (
    n BIGINT, 
    cath_id_1 VARCHAR,
    cath_id_2 VARCHAR,
    rupee_score NUMERIC,
    cecp_rmsd NUMERIC,
    cecp_tm_score NUMERIC,
    fatcat_rmsd NUMERIC,
    fatcat_tm_score NUMERIC
)
AS $$
BEGIN

    RETURN QUERY
    WITH results AS
    (
        SELECT
            ROW_NUMBER() OVER (PARTITION BY r.cath_id_1 ORDER BY r.rupee_score DESC, r.cath, r.solid) AS n,
            COUNT(*) OVER (PARTITION BY r.cath_id_1) AS tot,
            r.cath_id_1,
            r.cath_id_2,
            r.rupee_score,
            r.cecp_rmsd,
            r.cecp_tm_score,
            r.fatcat_rmsd,
            r.fatcat_tm_score
        FROM
            rupee_result r
    ),
    valid_results As
    (
        SELECT
            r.cath_id_1 AS cath_id
        FROM
            results r
        WHERE
            r.n = 1 AND r.cath_id_1 = r.cath_id_2 AND r.tot >= p_limit
    ),
    filtered_results AS
    (
        SELECT
            r.n, 
            r.cath_id_1,
            r.cath_id_2,
            r.rupee_score,
            r.cecp_rmsd,
            r.cecp_tm_score,
            r.fatcat_rmsd,
            r.fatcat_tm_score
        FROM 
            results r
            INNER JOIN valid_results v
                ON v.cath_id = r.cath_id_1 
        WHERE
            r.n <= p_limit
    )
    SELECT
        r.n,
        r.cath_id_1,
        r.cath_id_2,
        r.rupee_score,
        r.cecp_rmsd,
        r.cecp_tm_score,
        r.fatcat_rmsd,
        r.fatcat_tm_score
    FROM 
        filtered_results r
    ORDER BY
        r.cath_id_1,
        r.n;

END;
$$LANGUAGE plpgsql;


