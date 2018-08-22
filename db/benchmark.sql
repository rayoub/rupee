
CREATE TABLE benchmark
(
    name VARCHAR NOT NULl,
    db_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_benchmark_unique ON benchmark (name, db_id);
