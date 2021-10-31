
CREATE TABLE afdb_protein
(
    protein_sid SERIAL,
    proteome_id VARCHAR NOT NULL,
    file_name VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_afdb_protein_unique ON afdb_protein (protein_sid);

