#! /bin/bash

for id in $(find ./cath -name '*.results' -printf '%f\n'); do
    awk -v pdb_id=${id%.results} -e "BEGIN { OFS = \",\" } { print pdb_id, \$0 }" ./cath/${id}
done;

