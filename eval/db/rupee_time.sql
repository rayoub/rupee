
CREATE TABLE rupee_time
(
    version VARCHAR NULL,
    search_mode VARCHAR NULL,
    search_type VARCHAR NULL,
    db_id VARCHAR NOT NULL,
    time NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_rupee_time_unique ON rupee_time (version, search_mode, search_type, db_id);
