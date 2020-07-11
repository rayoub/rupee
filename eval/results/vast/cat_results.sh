#! /bin/bash

bm=$1
ver=$2
st=$3
dir=${bm}_${ver}_${st}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    awk -v ver=${ver} -v st=${st} -v db_id=${id%.txt} -e 'BEGIN { FS = ","; OFS = ","; } { print ver, NR, db_id, tolower(substr($1,1,4)) substr($1,6,1), $3, $5, st } ' ${dir}/${id}
done;

