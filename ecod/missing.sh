#! /bin/bash

# run like this 'xargs -a segments.txt -L1 ./missing.sh'

# output missing files that are expected by chopper

ecod_id=$1
seg_num=$2
pdb_id=$3
chain=$4
sres=$5
sins=$6
eres=$7
eins=$8

# output missing pdb files
if [ ! -e "../pdb/pdb/pdb${pdb_id}.ent.gz" ]; then
    echo ${pdb_id}
fi


