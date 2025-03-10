#! /bin/bash

ver=$1

# delete output directories if it already exist
[ -d ./pdb ] && rm -r pdb
[ -d ./obsolete ] && rm -r obsolete

# create output directory
mkdir ./pdb
mkdir ./obsolete

# message
echo "To the chopper! takes about 6 hours"

# parse pdb files
xargs -a pdb_${ver}.txt -L1 -P8 ./chopper.sh pdb
xargs -a obsolete_${ver}.txt -L1 -P8 ./chopper.sh obsolete

# message
echo "Copying files: takes about 10 minutes"

# copy already chopped files (aka bundles)
cp -n -r ../pdb/chopped/. ./pdb

echo "Done copying"

# move to db directory
cd ../../db

# prepare database (will prompt for password)
psql -d rupee -U postgres <<EOF
    truncate table chain;
    truncate table chain_grams;
    truncate table chain_hashes;
    \i x_chain.sql
EOF

# move to app directory
cd ../rupee-search/target

# import and hash
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i CHAIN
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h CHAIN

# done
echo
echo "Done"



