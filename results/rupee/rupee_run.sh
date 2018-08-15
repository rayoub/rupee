#! /bin/bash

bm=$1
ver=$2

# delete output directory if it exist
#[ -d ./${bm}_${ver} ] && rm -r ${bm}_${ver}

# create output directory
#mkdir ./${bm}_${ver}

xargs -a ../benchmarks/${bm}.txt -L1 ./rupee.sh ${bm} ${ver}
