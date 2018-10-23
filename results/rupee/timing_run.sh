#! /bin/bash

bm=$1
st=$2

cut -d, -f2 ../benchmarks/${bm}.txt | xargs -L1 ./timing.sh ${bm} ${st}

