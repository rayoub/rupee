
CREATE TABLE scop_background
(
    gram_1 INTEGER NOT NULL,
    gram_2 INTEGER NOT NULL,
    probability NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_scop_background_unique ON scop_background (gram_1, gram_2);


