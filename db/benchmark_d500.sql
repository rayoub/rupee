
CREATE TABLE benchmark_d500
(
    scop_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_benchmark_d500_unique ON benchmark_d500 (scop_id);
