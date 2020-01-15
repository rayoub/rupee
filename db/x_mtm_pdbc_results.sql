
-- important notes:
-- mTM does a better job of parsing therefore they will have some pdb entries that are missing from RUPEE.
-- To be fair to mTM we need to take the match they found and enter it into the alignment_scores table. 
-- mTM should be awarded for good parsing rather than inadvertantly filtering out unmatched data. 
-- When correctly normalized, I have not found a case where their TM-align score does not match RUPEE so this should be safe. 

TRUNCATE TABLE mtm_result;

COPY mtm_result (version, n, db_id_1, db_id_2, mtm_rmsd, mtm_tm_score) FROM '/home/ayoub/git/rupee/results/mtm/mtm_results.txt' WITH (DELIMITER ',');

