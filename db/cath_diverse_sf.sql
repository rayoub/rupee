
CREATE TABLE cath_diverse_sf
(
    cath VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_cath_diverse_sf_unique ON cath_diverse_sf (cath);
