
TRUNCATE rupee_result;

-- scop_d360 
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_rmsd.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_tm_score.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_fast.txt' WITH (DELIMITER ',');

-- scop_d62
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d62_scop_v1_73_rmsd.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d62_scop_v1_73_tm_score.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/scop_d62_scop_v1_73_fast.txt' WITH (DELIMITER ',');

-- cath_d99
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/cath_d99_cath_v4_2_0_rmsd.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/cath_d99_cath_v4_2_0_tm_score.txt' WITH (DELIMITER ',');

COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, sort) FROM '/home/ayoub/git/rupee/results/rupee/cath_d99_cath_v4_2_0_fast.txt' WITH (DELIMITER ',');




