
CREATE TABLE mtm_time
(
    db_id VARCHAR NOT NULL,
    time NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_mtm_time_unique ON mtm_time (db_id);
