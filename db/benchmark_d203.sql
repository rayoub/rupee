
CREATE TABLE benchmark_d203
(
    scop_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_benchmark_d203_unique ON benchmark_d203 (scop_id);
