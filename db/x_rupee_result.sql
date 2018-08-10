
TRUNCATE TABLE rupee_result;

COPY rupee_result (cath_id_1, cath_id_2, cath, solid, rupee_score, cecp_rmsd, cecp_tm_score, fatcat_rmsd, fatcat_tm_score) FROM PROGRAM 'sed "s/\s\s*/ /g" /home/ayoub/git/rupee/results/rupee.results | sed "s/\s\s*$//g"' WITH (DELIMITER ' ');
