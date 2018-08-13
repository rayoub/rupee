#! /bin/bash

dir=$1

for id in $(find ${dir} -name 'd*.txt' -printf '%f\n'); do
    awk -v scop_id=${id%.txt} -e 'BEGIN { OFS = ","; } $0 ~ "SCOP" { print $1, $16, $18, $5 }' ${dir}/${id}
done;

