#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name 'd*.txt' -printf '%f\n'); do
    awk -v ver=${ver} -v scop_id=${id%.txt} -e 'BEGIN { OFS = ","; } $0 ~ "SCOP" { print ver, $1, $16, $18, $5 }' ${dir}/${id}
done;

