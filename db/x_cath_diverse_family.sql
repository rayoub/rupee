
TRUNCATE cath_diverse_family_stage;
TRUNCATE cath_diverse_family;

COPY cath_diverse_family_stage (cath)
FROM '/home/ayoub/git/rupee/data/cath-diverse-families.txt';

INSERT INTO cath_diverse_family
SELECT
    d.*
FROM
    cath_diverse_family_stage f
    INNER JOIN cath_domain d 
        ON d.cath = f.cath
WHERE
    d.solid = '1.1.1.1.1';
