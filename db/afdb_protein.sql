
CREATE TABLE afdb_protein
(
    afdb_sid SERIAL,
    afdb_id VARCHAR NOT NULL,
    proteome_id VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_afdb_protein_unique ON afdb_protein (afdb_sid);

