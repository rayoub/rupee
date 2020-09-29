
TRUNCATE rupee_time;

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_all_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_top_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_fast_fl_timing.txt' WITH (DELIMITER ',');

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_all_ci_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_top_ci_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_chain_v08_28_2020_fast_ci_timing.txt' WITH (DELIMITER ',');

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_all_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_top_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_fast_fl_timing.txt' WITH (DELIMITER ',');

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_all_q_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_top_q_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v1_73_fast_q_timing.txt' WITH (DELIMITER ',');

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_all_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_top_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_fast_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/casp_d250_casp_scop_v2_07_opt_fl_timing.txt' WITH (DELIMITER ',');

COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_all_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_top_fl_timing.txt' WITH (DELIMITER ',');
COPY rupee_time (version, search_mode, search_type, db_id, time) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d360_scop_v2_07_fast_fl_timing.txt' WITH (DELIMITER ',');


