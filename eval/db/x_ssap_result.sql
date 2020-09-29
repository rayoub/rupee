TRUNCATE ssap_result_staged;
TRUNCATE ssap_result;

COPY ssap_result_staged (db_id_1, db_id_2, len_1, len_2, ssap_score, aligned_len, percent_overlap, percent_identity, rmsd) FROM '/home/ayoub/git/rupee/eval/results/ssap/ssap_output_default.txt' WITH (DELIMITER ' ');

INSERT INTO ssap_result-- (db_id_1, db_id_2, len_1, len_2, ssap_score, aligned_len, percent_overlap, percent_identity, rmsd)
WITH results AS
(
    SELECT
        db_id_1, 
        db_id_2, 
        len_1,
        len_2,
        ssap_score,
        aligned_len,
        percent_overlap,
        percent_identity,
        rmsd,
        ROW_NUMBER() OVER (PARTITION BY db_id_1, db_id_2) AS dupe_n
    FROM
        ssap_result_staged
)
SELECT
    db_id_1, 
    db_id_2, 
    len_1,
    len_2,
    ssap_score,
    aligned_len,
    percent_overlap,
    percent_identity,
    rmsd
FROM
    results
WHERE
    dupe_n = 1;

UPDATE alignment_scores 
SET 
    ssap_score = ssap_result.ssap_score
FROM 
    ssap_result
WHERE
    alignment_scores.version = 'casp_cath_v4_2_0' 
    AND alignment_scores.db_id_1 = ssap_result.db_id_1 AND alignment_scores.db_id_2 = ssap_result.db_id_2;

