#! /bin/bash

# create output directory
[ -d ./rupee ] || mkdir ./rupee

xargs -a ./cath-diverse-family-reps.txt -L1 ./rupee.sh
