#! /bin/bash

bm=$1
ver=$2

# delete output directory if it exist
[ -d ./${bm}-${ver} ] && rm -r ${bm}-${ver}

# create output directory
mkdir ./${bm}-${ver}

xargs -a ../benchmarks/${bm}.txt -L1 ./rupee.sh ${bm} ${ver}
