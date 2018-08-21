
COPY ssm_result (version, n, db_id_1, db_id_2, ssm_rmsd) FROM '/home/ayoub/git/rupee/results/ssm/ssm_results.txt' WITH (DELIMITER ',');
