
TRUNCATE ssm_time;

COPY ssm_time (db_id, time) FROM '/home/ayoub/git/rupee/eval/results/ssm/ssm_timing.txt' WITH (DELIMITER ',');


