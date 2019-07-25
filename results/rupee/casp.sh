#! /bin/bash

# this file needs to be edited for each type of benchmark

bm=$1
ver=$2
id=$3

java -jar ../../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s UPLOAD,CHAIN,../../data/casp/eu_preds/${id}.pdb,100,FALSE,FALSE,FALSE,FALSE,FALSE,FALSE,ALL_ALIGNED,TM_SCORE > ./${bm}_${ver}/${id}.txt
