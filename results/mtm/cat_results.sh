#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    awk -v ver=${ver} -v scop_id=${id%.txt} -e 'BEGIN { FS = ", "; OFS = ","; } NR > 1 { print ver, $1, scop_id, tolower($2), $4, $3 }' ${dir}/${id}
done;

