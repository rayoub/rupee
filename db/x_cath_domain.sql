
TRUNCATE cath_domain;

COPY cath_domain(cath_id, c, a, t, h, s, o, l, i, d, residue_count, resolution) FROM '/home/ayoub/git/rupee/data/cath/domains.txt' WITH (DELIMITER ' ');

-- aggregate some values for convenience
UPDATE cath_domain
SET 
    pdb_id = LEFT(cath_id,4),
    ca = c || '.' || a,
    cat = c || '.' || a || '.' || t,
    cath = c || '.' || a || '.' || t || '.' || h,
    solid = s || '.' || o || '.' || l || '.' || i || '.' || d,
    s35_rep = CASE WHEN ARRAY[o,l,i,d] = ARRAY[1,1,1,1] THEN TRUE ELSE FALSE END;

-- mark topology reps (cath_name table must be imported first)
UPDATE cath_domain 
SET
    topology_rep = TRUE
FROM
    cath_name WHERE cath_name.cath_name = cath_domain.cat AND cath_name.cath_id = cath_domain.cath_id;

-- mark superfamily reps (cath_name table must be imported first)
UPDATE cath_domain 
SET
    superfamily_rep = TRUE
FROM
    cath_name WHERE cath_name.cath_name = cath_domain.cath AND cath_name.cath_id = cath_domain.cath_id;




