#! /bin/bash

pdb_id=$1

java -jar ../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -s CATH,CATH,${pdb_id},1,50,400,FALSE,FALSE,TRUE,FALSE,FALSE,FALSE,CE,TM_SCORE > ./rupee/${pdb_id}.results
