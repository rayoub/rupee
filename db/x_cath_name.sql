
TRUNCATE cath_name;

COPY cath_name(cath_name, cath_id, description) FROM '/home/ayoub/git/rupee/cath/names.txt' WITH (DELIMITER '#');
