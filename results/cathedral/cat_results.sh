#! /bin/bash

dir=$1

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    awk -v id=${id%.txt} -e 'BEGIN { OFS = "," } { print NR, id, $0 }' ${dir}/${id}
done;

