
TRUNCATE benchmark_d193;
TRUNCATE benchmark_d203;
TRUNCATE benchmark_d499;
TRUNCATE benchmark_d500;
TRUNCATE benchmark_diverse_family;

COPY benchmark_d193 (scop_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d193.txt';
COPY benchmark_d203 (scop_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d203.txt';
COPY benchmark_d499 (scop_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d499.txt';
COPY benchmark_d500 (scop_id) FROM '/home/ayoub/git/rupee/results/benchmarks/scop_d500.txt';
COPY benchmark_diverse_family (cath_id) FROM '/home/ayoub/git/rupee/results/benchmarks/cath_diverse_family.txt';






