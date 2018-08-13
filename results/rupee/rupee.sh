#! /bin/bash

cath_id=$1

java -jar ../../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s CATH,CATH,${cath_id},400,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,CE,TM_SCORE > ./temp/${cath_id}.txt
