
CREATE TABLE dir_chain
(
    db_sid SERIAL,
    db_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_dir_chain_unique ON dir_chain (db_sid);
