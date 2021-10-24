
TRUNCATE scop_domain;

COPY scop_domain (scop_id, pdb_id, sunid, cl, cf, sf, fa) FROM 'C:\git\rupee\data\scop\domains.txt' WITH (DELIMITER ' ');

-- aggregate some values for convenience
UPDATE scop_domain
SET 
    cl_cf = cl || '.' || cf,
    cl_cf_sf = cl || '.' || cf || '.' || sf,
    cl_cf_sf_fa = cl || '.' || cf || '.' || sf || '.' || fa;

-- assign sort key
UPDATE scop_domain 
SET 
    sort_key = 
        LPAD(cl, 1, '0') || '.' || 
        LPAD(cf || '', 3, '0') || '.' || 
        LPAD(sf || '', 2, '0') || '.' || 
        LPAD(fa || '', 2, '0') || '.' || 
        scop_id; 




