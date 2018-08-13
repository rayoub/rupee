#! /bin/bash

pdb_id=$1

java -jar ../rupee-mgr/target/rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -c CATH,${pdb_id}
