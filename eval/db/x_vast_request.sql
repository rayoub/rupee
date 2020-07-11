
TRUNCATE vast_request;

COPY vast_request (db_id, request_id) FROM '/home/ayoub/git/rupee/eval/results/vast/requests.txt' WITH (DELIMITER ',');
