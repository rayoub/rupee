
TRUNCATE scop_name;

COPY scop_name(scop_name, description) FROM 'C:\git\rupee\data\scop\names.txt' WITH (DELIMITER '#');
