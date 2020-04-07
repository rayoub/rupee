
TRUNCATE TABLE ssm_result_staged;
TRUNCATE TABLE ssm_result;

COPY ssm_result_staged (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score, search_type) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results_q_score.txt' WITH (DELIMITER ',');
COPY ssm_result_staged (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score, search_type) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results_rmsd.txt' WITH (DELIMITER ',');

INSERT INTO ssm_result (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score, search_type)
WITH results AS
(
    SELECT
        version, 
        search_type, 
        n,
        db_id_1,
        db_id_2,
        ssm_rmsd,
        ssm_q_score,
        ROW_NUMBER() OVER (PARTITION BY version, search_type, db_id_1, db_id_2 ORDER BY n) AS dupe_n
    FROM
        ssm_result_staged
),
filtered_results AS
(
    SELECT
        version, 
        search_type, 
        ROW_NUMBER() OVER (PARTITION BY version, search_type, db_id_1 ORDER BY n) AS dedupe_n,
        db_id_1,
        db_id_2,
        ssm_rmsd,
        ssm_q_score
    FROM
        results
    WHERE
        dupe_n = 1
)
SELECT
    version,
    dedupe_n AS n,
    db_id_1,
    db_id_2,
    ssm_rmsd,
    ssm_q_score,
    search_type
FROM
    filtered_results
WHERE
    dedupe_n <= 100
ORDER BY
    version, 
    search_type, 
    db_id_1, 
    dedupe_n;

