
CREATE TABLE search_queue
(
    search_id SERIAL,
    user_id VARCHAR NOT NULL,
    db_type INTEGER NOT NULL,
    search_filter INTEGER NOT NULL,
    search_by INTEGER NOT NULL,
    db_id VARCHAR NOT NULL,
    upload_id INTEGER NOT NULL,
    sort_by INTEGER NOT NULL,
    max_records INTEGER NOT NULL,
    status VARCHAR NOT NULL DEFAULT ('pending'),
    inserted_on TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0)
);

CREATE UNIQUE INDEX idx_search_queue_unique ON search_queue (search_id);

