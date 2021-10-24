
TRUNCATE ecod_domain;

COPY ecod_domain (ecod_id, pdb_id, x, h, t, f, architecture, x_description, h_description, t_description, f_description) FROM 'C:\git\rupee\data\ecod\domains.txt' WITH (DELIMITER '#');

-- assign sort key
UPDATE ecod_domain 
SET 
    sort_key = 
        LPAD(x, 4, '0') || '.' || 
        LPAD(h, 3, '0') || '.' || 
        LPAD(t, 2, '0') || '.' || 
        LPAD(f, 4, '0') || '.' || 
        ecod_id; 







