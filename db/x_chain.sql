
TRUNCATE chain;

COPY chain (chain_id, pdb_id, chain_name, residue_count) FROM 'D:\git\rupee\data\chain\pdb_v01_15_2025.txt' WITH (DELIMITER ' ');

-- assign sort key
UPDATE chain SET sort_key = chain_id;
