#! /bin/bash

# this file needs to be edited for each type of benchmark

bm=$1
ver=$2
id=$3

java -jar ../../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s UPLOAD,CATH,../../data/casp/eu_preds/${id}.pdb,400,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,TOP_ALIGNED,RMSD > ./${bm}_${ver}/${id}.txt

