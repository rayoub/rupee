#! /bin/bash

# this file needs to be edited for each type of benchmark

bm=$1
st=$2
id=$3

java -jar ../../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s DB_ID,CATH,CATH,${id},100,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,FAST,RMSD >> ./${bm}_timing_${st}.txt
