#! /bin/bash

# this file needs to be edited for each type of benchmark

bm=$1
id=$2

java -jar ../../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s DB_ID,CATH,CATH,${id},400,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,CE,TM_SCORE > ./${bm}/${id}.txt
