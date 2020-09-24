
TRUNCATE TABLE mtm_result;

COPY mtm_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/eval/results/mtm/mtm_results.txt' WITH (DELIMITER ',');

DELETE FROM mtm_result WHERE n > 100;


