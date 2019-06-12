
CREATE TABLE cath_grams
(
    cath_id VARCHAR NOT NULL,
    grams INTEGER ARRAY NOT NULL,
    coords REAL ARRAY NOT NULL
);

CREATE UNIQUE INDEX idx_cath_grams_unique ON cath_grams (cath_id);

