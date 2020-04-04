
CREATE TABLE vast_request
(
    db_id VARCHAR NOT NULL,
    request_id VARCHAR NOT NULL
); 

CREATE UNIQUE INDEX idx_vast_request_unique ON vast_request (db_id);
