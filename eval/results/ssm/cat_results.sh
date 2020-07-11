#! /bin/bash

bm=$1
ver=$2
st=$3
dir=${bm}_${ver}_${st}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
   awk -v ver=${ver} -v st=${st} -v db_id=${id%.txt} -e 'BEGIN { OFS = ","; } $0 ~ "SCOP" { print ver, $1, db_id, $17, $5, $2, st }' ${dir}/${id}
done;

