
TRUNCATE benchmark;

COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d193.txt' WITH (DELIMITER ',');

COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d500.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d499.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d421.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d100.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d50.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d3.txt' WITH (DELIMITER ',');

COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/cath_diverse_family.txt' WITH (DELIMITER ',');






