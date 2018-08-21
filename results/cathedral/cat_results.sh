#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    awk -v ver=${ver} -v id=${id%.txt} -e 'BEGIN { OFS = "," } { print ver, NR, id, $0 }' ${dir}/${id}
done;

