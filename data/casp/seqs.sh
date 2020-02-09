#! /bin/bash

# run like this 'xargs -a seqs_*.txt -L1 ./seqs.sh'

seq=$1
hhblits -cpu 8 -i ./seqs/${seq}.seq -d ~/git/hh-suite/databases/UniRef30_2020_01/UniRef30_2020_01 -o ./seqs/${seq}.hhr -n 3 -cov 60

