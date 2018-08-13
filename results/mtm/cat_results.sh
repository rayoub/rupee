#! /bin/bash

dir=$1

for id in $(find ${dir} -name 'd*.txt' -printf '%f\n'); do
    awk -v scop_id=${id%.txt} -e 'BEGIN { FS = ", "; OFS = ","; } NR > 1 { print $1, scop_id, $2, $4, $3 }' ${dir}/${id}
done;

