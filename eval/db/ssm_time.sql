
CREATE TABLE ssm_time
(
    db_id VARCHAR NOT NULL,
    time NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_ssm_time_unique ON ssm_time (db_id);
