
TRUNCATE chain;

COPY chain (chain_id, pdb_id, residue_count) FROM '/home/ayoub/git/rupee/data/chain/chain_list_v07_18_2018.txt' WITH (DELIMITER ' ');

-- assign sort key
UPDATE chain SET sort_key = chain_id;
