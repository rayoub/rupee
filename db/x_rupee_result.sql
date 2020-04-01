
TRUNCATE rupee_result;

-- scop_d360
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_all_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/scop_d360_scop_v2_07_top_fl.txt' WITH (DELIMITER ',');

-- casp_d250
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_chain_v01_01_2020_all_ci.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_chain_v01_01_2020_top_ci.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_scop_v2_07_all_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_scop_v2_07_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_scop_v1_73_all_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_scop_v1_73_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_all_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_top_fl.txt' WITH (DELIMITER ',');

-- cathedral with casp_d250 
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_top_rmsd.txt' WITH (DELIMITER ',');

-- cathedral with casp_d250 (additional fileds)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_all_rmsd.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_top_ssap.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/results/rupee/casp_d250_casp_cath_v4_2_0_all_ssap.txt' WITH (DELIMITER ',');



