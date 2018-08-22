
CREATE OR REPLACE FUNCTION insert_rupee_eval (p_benchmark VARCHAR, p_version VARCHAR, p_description VARCHAR, p_limit INTEGER) RETURNS VOID
AS $$
BEGIN

    WITH results AS
    (
        SELECT
            n,
            db_id_1,
            db_id_2,
            ce_rmsd,
            ce_tm_score
        FROM
            get_rupee_results(p_benchmark, p_version, p_limit)
    ),
    eval AS
    (
        SELECT
            AVG(ce_rmsd) AS avg_ce_rmsd,
            AVG(ce_tm_score) AS avg_ce_tm_score
        FROM
            results
    )
    INSERT INTO rupee_eval(benchmark, version, description, avg_ce_rmsd, avg_ce_tm_score, num_rows)
    SELECT
        p_benchmark,
        p_version,
        p_description,
        e.avg_ce_rmsd,
        e.avg_ce_tm_score,
        p_limit
    FROM
        eval e;

END;
$$LANGUAGE plpgsql;


