
TRUNCATE chain;

COPY chain (chain_id, pdb_id, chain_name, residue_count) FROM '/home/ayoub/git/rupee/data/chain/pdb_v06_26_2019.txt' WITH (DELIMITER ' ');

-- assign sort key
UPDATE chain SET sort_key = chain_id;
