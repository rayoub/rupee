
TRUNCATE vast_done;

COPY vast_done (db_id) FROM '/home/ayoub/git/rupee/results/vast/vast_done.txt' WITH (DELIMITER ',');
