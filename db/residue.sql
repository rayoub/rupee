
CREATE TABLE residue
(
    pdb_id VARCHAR NOT NULL,
    chain_id VARCHAR NOT NULL,
    atom_number INTEGER NOT NULL,
    residue_number INTEGER NOT NULL,
    insert_code VARCHAR NULL,
    residue_code VARCHAR NOT NULL, 
    ssa VARCHAR NOT NULL,
    sse VARCHAR NOT NULL,
    phi NUMERIC NULL,
    psi NUMERIC NULL,
    descriptor INTEGER NOT NULL,
    run_factor INTEGER NOT NULL,
    gram INTEGER NOT NULL,
    break_before INTEGER NOT NULL,
    break_after INTEGER NOT NULL
);

CREATE UNIQUE INDEX idx_residue_unique ON residue (pdb_id, chain_id, atom_number);
