#! /bin/bash

bm=$1
ver=$2
dir=${bm}_${ver}

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    xargs -a ${dir}/${id} -L1 printf "%s,%s\n" ${ver}
done;

