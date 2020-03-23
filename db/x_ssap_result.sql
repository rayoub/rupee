
COPY ssap_result (db_id_1, db_id_2, len_1, len_2, ssap_score, aligned_len, percent_overlap, percent_identity, rmsd) FROM '/home/ayoub/git/rupee/results/ssap/ssap_output.txt' WITH (DELIMITER ' ');

