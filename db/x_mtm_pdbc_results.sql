
-- important notes:
-- mTM does a better job of parsing therefore they will have some pdb entries that are missing from RUPEE.
-- To be fair to mTM we need to take the matches they found and enter them into the alignment_scores table. 
-- mTM should be awarded for good parsing rather than inadvertantly filtering out unmatched data. 
-- When correctly normalized, I have not found a case where their TM-align score does not match RUPEE so this should be safe. 

TRUNCATE TABLE mtm_result;

COPY mtm_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/results/mtm/mtm_results.txt' WITH (DELIMITER ',');

INSERT INTO alignment_scores (version, db_id_1, db_id_2, tm_q_rmsd, tm_q_tm_score)
WITH filtered AS
(
    SELECT
        r.*
    FROM
        mtm_result r
        LEFT JOIN alignment_scores s
            ON r.version = s.version
            AND r.db_id_1 = s.db_id_1
            AND r.db_id_2 = s.db_id_2
    WHERE
        s.version IS NULL
        AND r.n <= 100
)
SELECT
    version,
    db_id_1,
    db_id_2,
    mtm_rmsd,
    mtm_tm_score
FROM
    filtered;

