
COPY cathedral_result (version, n, db_id_1, db_id_2, ssap_score, cathedral_rmsd) FROM '/home/ayoub/git/rupee/results/cathedral/cathedral_results.txt' WITH (DELIMITER ',');
