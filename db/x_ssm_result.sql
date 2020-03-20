
TRUNCATE TABLE ssm_result;

COPY ssm_result (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results.txt' WITH (DELIMITER ',');

DELETE FROM ssm_result WHERE n > 100;

