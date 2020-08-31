#! /bin/bash

# delete output directories if it already exist
[ -d ./pdb ] && rm -r pdb
[ -d ./obsolete ] && rm -r obsolete

# create output directory
mkdir ./pdb
mkdir ./obsolete

# parse pdb files
xargs -a pdb_v06_19_2020.txt -L1 -P8 ./chopper.sh pdb
xargs -a obsolete_v06_19_2020.txt -L1 -P8 ./chopper.sh obsolete

# move to db directory
cd ../../db

# prepare database (will prompt for password)
psql -d rupee <<EOF
    truncate table chain;
    truncate table chain_grams;
    truncate table chain_hashes;
    \i x_chain.sql
EOF

## move to app directory
cd ../rupee-search/target

## import and hash
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i CHAIN
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h CHAIN


