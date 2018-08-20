
CREATE TABLE benchmark_diverse_family
(
    cath_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_benchmark_diverse_family_unique ON benchmark_diverse_family (cath_id);
