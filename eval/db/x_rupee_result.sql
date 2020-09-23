
TRUNCATE rupee_result;

-- rupee vs. rupee with scop_d360 (full-length)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_fast_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_all_fl.txt' WITH (DELIMITER ',');
-- COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_opt_fl.txt' WITH (DELIMITER ',');

-- ruppe vs. rupee with casp_d250 (full-length)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_fast_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_all_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_opt_fl.txt' WITH (DELIMITER ',');

-- mtm with casp_d250 (contained-in)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_fast_ci.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_top_ci.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_all_ci.txt' WITH (DELIMITER ',');

-- cathedral with casp_d250 (full-length)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_fast_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_all_fl.txt' WITH (DELIMITER ',');

-- cathedral with casp_d250 (ssap)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_fast_ssap.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_top_ssap.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_cath_v4_2_0_all_ssap.txt' WITH (DELIMITER ',');

-- ssm with casp_d250 (full-length)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_fast_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_all_fl.txt' WITH (DELIMITER ',');

-- ssm with casp_d250 (q-score)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_fast_q.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_top_q.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_all_q.txt' WITH (DELIMITER ',');

-- vast with casp_d250 (full-length)
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_fast_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_top_fl.txt' WITH (DELIMITER ',');
COPY rupee_result (version, n, db_id_1, db_id_2, rupee_rmsd, rupee_tm_score, rupee_q_score, rupee_ssap_score, search_mode, search_type) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_all_fl.txt' WITH (DELIMITER ',');


