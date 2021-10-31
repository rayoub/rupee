
CREATE TABLE afdb_proteome
(
    proteome_id VARCHAR NOT NULL,
    species VARCHAR NOT NULL,
    common_name VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_afdb_proteome_unique ON afdb_proteome (proteome_id);

