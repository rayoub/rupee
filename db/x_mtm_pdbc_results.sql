

TRUNCATE TABLE mtm_pdbc_result;
TRUNCATE TABLE mtm_pdbc_result_matched;
TRUNCATE TABLE mtm_pdbc_result_unmatched;

COPY mtm_pdbc_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/results/mtm/mtm_pdbc_results.txt' WITH (DELIMITER ',');
