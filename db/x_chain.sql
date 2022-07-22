
TRUNCATE chain;

COPY chain (chain_id, pdb_id, chain_name, residue_count) FROM 'C:\git\rupee\data\chain\pdb_v07_16_2022.txt' WITH (DELIMITER ' ');

-- assign sort key
UPDATE chain SET sort_key = chain_id;
