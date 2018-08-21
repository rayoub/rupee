
COPY rupee_result (version, n, cath_id_1, cath_id_2, ce_rmsd, ce_tm_score) FROM PROGRAM 'sed "s/\s\s*/ /g" /home/ayoub/git/rupee/results/rupee/rupee_results.txt' WITH (DELIMITER ',');
