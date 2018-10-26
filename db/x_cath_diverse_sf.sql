
TRUNCATE cath_diverse_sf;

COPY cath_diverse_sf (cath) FROM '/home/ayoub/git/rupee/data/cath/cath-diverse-sf-v4_2_0.txt' WITH (DELIMITER ' ');

