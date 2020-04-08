
TRUNCATE benchmark;

-- ****************************************************************
-- benchmarks from the 2019 PLoS ONE paper
-- ****************************************************************

COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d500.txt' WITH (DELIMITER ',');

-- mtm specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d499.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d360.txt' WITH (DELIMITER ',');

-- ssm specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d204.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d193.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d62.txt' WITH (DELIMITER ',');

-- cathedral specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/cath_d100.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/cath_d99.txt' WITH (DELIMITER ',');


-- ****************************************************************
-- benchmarks for future paper
-- ****************************************************************

COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/casp_d250.txt' WITH (DELIMITER ',');

-- mtm specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/casp_mtm_d246.txt' WITH (DELIMITER ',');

-- ssm specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/casp_ssm_d240.txt' WITH (DELIMITER ',');
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/casp_ssm_d233.txt' WITH (DELIMITER ',');

-- cathedral specific
COPY benchmark (name, db_id) FROM '/home/ayoub/git/rupee/results/benchmarks/casp_cathedral_d247.txt' WITH (DELIMITER ',');

