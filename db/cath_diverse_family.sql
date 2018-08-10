
CREATE TABLE cath_diverse_family
(
    domain_id INTEGER NOT NULL,
    cath_id VARCHAR NOT NULL,
    cath VARCHAR NOT NULL,
    solid VARCHAR NOT NULL,
    s35 BOOLEAN NOT NULL,
    residue_count INTEGER NOT NULL,
    resolution NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_cath_diverse_family_unique ON cath_diverse_family (domain_id);

