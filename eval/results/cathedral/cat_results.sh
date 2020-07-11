#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    tail ${dir}/${id} -n+8 | awk -v ver=${ver} -v id=${id%.txt} \
        'BEGIN { 
            FS = "[ \n]+"; 
            RS = "Show related domains\n"; 
            OFS = "," 
        } 
        { print ver, NR, id, $1, $4, $5 }'
done;

