#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
   awk -v ver=${ver} -v db_id=${id%.txt} -e 'BEGIN { OFS = ","; } $0 ~ "SCOP" { print ver, $1, db_id, $17, $5, $2 }' ${dir}/${id}
done;

