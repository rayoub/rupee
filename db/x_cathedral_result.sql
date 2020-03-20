
TRUNCATE TABLE cathedral_result;

COPY cathedral_result (version, n, db_id_1, db_id_2, cathedral_rmsd, cathedral_ssap) FROM '/home/ayoub/git/rupee/results/cathedral/cathedral_results.txt' WITH (DELIMITER ',');

DELETE FROM cathedral_result WHERE n > 100;
