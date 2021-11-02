
TRUNCATE afdb_protein;

COPY afdb_protein(afdb_id, proteome_id) FROM 'C:\git\rupee\data\afdb\protein.txt' WITH (DELIMITER ',', ENCODING 'UTF8');