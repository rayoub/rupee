
--COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/rupee_results.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_tm_score.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_rmsd.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_fast.txt' WITH (DELIMITER ',');


