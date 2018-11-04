
TRUNCATE response_time;

COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/mtm/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/ssm/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/cathedral/cath_d99_timing.txt' WITH (DELIMITER ',');


COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/rupee/scop_d62_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/rupee/scop_d62_timing_fast.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/rupee/cath_d99_timing.txt' WITH (DELIMITER ',');
COPY response_time (app, benchmark, db_id, timing) FROM '/home/ayoub/git/rupee/results/rupee/cath_d99_timing_fast.txt' WITH (DELIMITER ',');

