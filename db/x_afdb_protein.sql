
TRUNCATE afdb_protein;

COPY afdb_protein(proteome_id, file_name) FROM 'C:\git\rupee\data\afdb\protein.txt' WITH (DELIMITER ',', ENCODING 'UTF8');