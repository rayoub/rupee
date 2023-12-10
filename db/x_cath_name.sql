
TRUNCATE cath_name;

COPY cath_name(cath_name, cath_id, description) FROM 'D:\git\rupee\data\cath\names.txt' WITH (DELIMITER '#');
