#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    awk -v ver=${ver} -v db_id=${id%.txt} -e 'BEGIN { FS = ","; OFS = ","; } { print ver, NR, db_id, tolower(substr($1,1,4)) substr($1,6,1), $3, $5 } NR==100 { exit }' ${dir}/${id}
done;

