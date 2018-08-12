#! /bin/bash

dir=$1

for id in $(find ${dir} -name 'd*.txt' -printf '%f\n'); do
    awk -v scop_id=${id%.txt} -e 'BEGIN { OFS = ", " } { print scop_id, $0 }' ${dir}/${id}
done;

# todo: strip extra spaces, output only necessary fields, strip headers

