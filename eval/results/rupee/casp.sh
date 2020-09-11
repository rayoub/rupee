#! /bin/bash

# this file needs to be edited for each type of benchmark

bm=$1
ver=$2
id=$3

java -jar ../../rupee-eval/target/rupee-eval-0.0.1-SNAPSHOT-jar-with-dependencies.jar -u CHAIN,../../../data/casp/eu_preds/${id}.pdb,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,ALL_ALIGNED,FULL_LENGTH > ./${bm}_${ver}/${id}.txt

