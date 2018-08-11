#! /bin/bash

dir=$1

for id in $(find ${dir} -name '*.results' -printf '%f\n'); do
    awk -v pdb_id=${id%.results} -e "BEGIN { OFS = \",\" } { print pdb_id, \$0 }" ${dir}/${id}
done;

