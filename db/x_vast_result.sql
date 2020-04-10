
TRUNCATE TABLE vast_result;

COPY vast_result (version, n, db_id_1, db_id_2, vast_score, vast_rmsd) FROM '/home/ayoub/git/rupee/results/vast/vast_results.txt' WITH (DELIMITER ',');

