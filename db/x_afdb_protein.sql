
TRUNCATE afdb_protein;

COPY afdb_protein(afdb_id, proteome_id) FROM 'D:\git\rupee\data\afdb\protein.txt' WITH (DELIMITER ',', ENCODING 'UTF8');