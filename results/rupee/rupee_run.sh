#! /bin/bash

bm=$1

# delete output directory if it exist
[ -d ./${bm} ] && rm -r ${bm}

# create output directory
mkdir ./${bm}

xargs -a ../benchmarks/${bm}.txt -L1 ./rupee.sh ${bm}
