
TRUNCATE scop_name;

COPY scop_name(scop_name, description) FROM '/home/ayoub/git/rupee/data/scop/names.txt' WITH (DELIMITER '#');
