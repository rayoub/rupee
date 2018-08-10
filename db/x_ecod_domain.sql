
TRUNCATE ecod_domain;

COPY ecod_domain (ecod_id, pdb_id, x, h, t, f, architecture, x_description, h_description, t_description, f_description) FROM '/home/ayoub/git/rupee/ecod/domains.txt' WITH (DELIMITER '#');







