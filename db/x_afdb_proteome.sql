
TRUNCATE afdb_proteome;

COPY afdb_proteome(proteome_id, species, common_name) FROM 'D:\git\rupee\data\afdb\proteome.txt' WITH (DELIMITER ',', ENCODING 'UTF8');