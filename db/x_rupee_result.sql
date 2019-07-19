
TRUNCATE rupee_result;

-- scop_d360
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_all.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_top.txt' WITH (DELIMITER ',');

-- casp_d150
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/casp_d150_casp_cath_v4_2_0_all.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/casp_d150_casp_chain_v6_26_2019_all.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/casp_d150_casp_scop_v2_07_all.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode) FROM '/home/ayoub/git/rupee/results/rupee/casp_d150_casp_scop_v2_07_top.txt' WITH (DELIMITER ',');




