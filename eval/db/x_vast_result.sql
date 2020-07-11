
TRUNCATE TABLE vast_result_staged;
TRUNCATE TABLE vast_result;

COPY vast_result_staged (version, n, db_id_1, db_id_2, vast_score, vast_rmsd, search_type) FROM '/home/ayoub/git/rupee/eval/results/vast/vast_results_fl.txt' WITH (DELIMITER ',');
COPY vast_result_staged (version, n, db_id_1, db_id_2, vast_score, vast_rmsd, search_type) FROM '/home/ayoub/git/rupee/eval/results/vast/vast_results_rmsd.txt' WITH (DELIMITER ',');

INSERT INTO vast_result (version, n, db_id_1, db_id_2, vast_score, vast_rmsd, search_type)
WITH results AS
(
    SELECT
        version, 
        search_type,
        n,
        db_id_1,
        db_id_2,
        vast_score,
        vast_rmsd,
        ROW_NUMBER() OVER (PARTITION BY version, search_type, db_id_1, db_id_2 ORDER BY n) AS dupe_n
    FROM
        vast_result_staged
),
filtered_results AS
(
    SELECT
        version, 
        search_type,
        ROW_NUMBER() OVER (PARTITION BY version, search_type, db_id_1 ORDER BY n) AS dedupe_n,
        db_id_1,
        db_id_2,
        vast_score,
        vast_rmsd
    FROM
        results
    WHERE
        dupe_n = 1
)
SELECT
    version,
    dedupe_n AS n,
    db_id_1,
    db_id_2,
    vast_score,
    vast_rmsd,
    search_type
FROM
    filtered_results
WHERE
    dedupe_n <= 100
ORDER BY
    version, 
    search_type,
    db_id_1, 
    dedupe_n;

