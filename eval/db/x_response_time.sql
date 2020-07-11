
TRUNCATE response_time;

COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/mtm/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/ssm/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/cathedral/cath_d99_timing.txt' WITH (DELIMITER ',');


COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d62_timing_fast.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/cath_d99_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/cath_d99_timing_fast.txt' WITH (DELIMITER ',');

-- used for comparing rupee against rupee
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d62_timing_1.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/eval/results/rupee/scop_d62_timing_2.txt' WITH (DELIMITER ',');


