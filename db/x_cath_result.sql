
COPY cath_result (cath_id_1, cath_id_2, ssap_score, rmsd) FROM PROGRAM 'sed "s/\s\s*/ /g" /home/ayoub/git/structure/results/cath.results' WITH (DELIMITER ',');
