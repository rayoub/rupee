
CREATE TABLE scop_pair
(
    sid SERIAL,
    db_id_1 VARCHAR NOT NULL,
    db_id_2 VARCHAR NOT NULL,
    similarity NUMERIC NOT NULL,
    CHECK(db_id_1 < db_id_2)
);

CREATE UNIQUE INDEX idx_scop_pair_unique ON scop_pair (db_id_1, db_id_2);

