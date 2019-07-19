

TRUNCATE TABLE mtm_result;
TRUNCATE TABLE mtm_result_matched;
TRUNCATE TABLE mtm_result_unmatched;

COPY mtm_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/results/mtm/mtm_results.txt' WITH (DELIMITER ',');

-- matched
WITH structures AS
(
    SELECT
        chain_id
    FROM
        chain_grams
),
filtered AS
(
    SELECT
        r.version,
        r.n,
        r.db_id_1,
        r.db_id_2,
        r.mtm_rmsd,
        r.mtm_tm_score
    FROM 
        mtm_result r
        INNER JOIN structures s
            ON s.chain_id = r.db_id_2
)
INSERT INTO mtm_result_matched
SELECT
    version,
    ROW_NUMBER() OVER (PARTITION BY version, db_id_1 ORDER BY n) AS n,
    db_id_1,
    db_id_2,
    mtm_rmsd,
    mtm_tm_score
FROM
    filtered;

-- unmatched
WITH structures AS
(
    SELECT
        chain_id
    FROM
        chain_grams
)
INSERT INTO mtm_result_unmatched
SELECT
    r.*
FROM 
    mtm_result r
    LEFT JOIN structures s
        ON s.chain_id = r.db_id_2
WHERE
    s.chain_id IS NULL;


