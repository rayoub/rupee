
TRUNCATE cath_name;

COPY cath_name(cath_name, cath_id, description) FROM 'C:\git\rupee\data\cath\names.txt' WITH (DELIMITER '#');
