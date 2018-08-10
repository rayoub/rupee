
CREATE OR REPLACE FUNCTION set_cath_eval (p_description VARCHAR, p_limit INTEGER) RETURNS VOID
AS $$
BEGIN

    WITH results AS
    (
        SELECT
            n,
            cath_id_1,
            cath_id_2,
            cecp_rmsd,
            cecp_tm_score,
            fatcat_rmsd,
            fatcat_tm_score
        FROM
            get_cath_results(p_limit)
    ),
    eval AS
    (
        SELECT
            AVG(cecp_rmsd) AS avg_cecp_rmsd,
            AVG(cecp_tm_score) AS avg_cecp_tm_score,
            AVG(fatcat_rmsd) avg_fatcat_rmsd,
            AVG(fatcat_tm_score) avg_fatcat_tm_score
        FROM
            results
    )
    INSERT INTO cath_eval(description, avg_cecp_rmsd, avg_cecp_tm_score, avg_fatcat_rmsd, avg_fatcat_tm_score, num_rows)
    SELECT
        p_description,
        e.avg_cecp_rmsd,
        e.avg_cecp_tm_score,
        e.avg_fatcat_rmsd,
        e.avg_fatcat_tm_score,
        p_limit
    FROM
        eval e;

END;
$$LANGUAGE plpgsql;


