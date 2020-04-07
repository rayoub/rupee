
TRUNCATE TABLE ssm_result;

COPY ssm_result (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score, search_type) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results_q_score.txt' WITH (DELIMITER ',');
COPY ssm_result (version, n, db_id_1, db_id_2, ssm_rmsd, ssm_q_score, search_type) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results_rmsd.txt' WITH (DELIMITER ',');

DELETE FROM ssm_result WHERE n > 100;

