

TRUNCATE TABLE mtm_dom_result;
TRUNCATE TABLE mtm_dom_result_matched;
TRUNCATE TABLE mtm_dom_result_unmatched;

COPY mtm_dom_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/results/mtm/mtm_dom_results.txt' WITH (DELIMITER ',');

-- normalize
UPDATE mtm_dom_result SET db_id_2 = LOWER(db_id_2);

-- matched
WITH structures AS
(
    SELECT
        scop_id
    FROM
        scop_grams
),
filtered AS
(
    SELECT
        r.version,
        r.n,
        r.db_id_1,
        r.db_id_2,
        r.mtm_rmsd,
        r.mtm_tm_score,
        r.ce_rmsd,
        r.ce_tm_score
    FROM 
        mtm_dom_result r
        INNER JOIN structures s
            ON s.scop_id = r.db_id_2
)
INSERT INTO mtm_dom_result_matched
SELECT
    version,
    ROW_NUMBER() OVER (PARTITION BY version, db_id_1 ORDER BY n) AS n,
    db_id_1,
    db_id_2,
    mtm_rmsd,
    mtm_tm_score,
    ce_rmsd,
    ce_tm_score
FROM
    filtered;

-- unmatched
WITH structures AS
(
    SELECT
        scop_id
    FROM
        scop_grams
)
INSERT INTO mtm_dom_result_unmatched
SELECT
    r.*
FROM 
    mtm_dom_result r
    LEFT JOIN structures s
        ON s.scop_id = r.db_id_2
WHERE
    s.scop_id IS NULL
    AND r.db_id_2 NOT LIKE 'p%';


