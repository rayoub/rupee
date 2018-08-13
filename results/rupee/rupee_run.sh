#! /bin/bash

# delete temp directory if it exist
[ -d ./temp ] && rm -r temp

# create temp directory
mkdir ./temp

xargs -a ../benchmarks/cath-diverse-family-reps.txt -L8 ./rupee.sh
