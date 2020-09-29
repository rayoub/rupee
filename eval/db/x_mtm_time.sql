
TRUNCATE mtm_time;

COPY mtm_time (db_id, time) FROM '/home/ayoub/git/rupee/eval/results/mtm/mtm_timing.txt' WITH (DELIMITER ',');


