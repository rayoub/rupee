
CREATE TABLE eu_neff
(
    target VARCHAR NOT NULl,
    domain VARCHAR NOT NULl,
    neff NUMERIC NOT NULL
);

CREATE UNIQUE INDEX idx_eu_neff_unique ON eu_neff (target, domain);
