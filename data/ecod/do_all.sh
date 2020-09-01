#! /bin/bash

ver=develop240

# delete output directory if it already exist
[ -d ./pdb ] && rm -r pdb

# create output directory
mkdir ./pdb

# parse definition files
awk -f domains.awk ecod.${ver}.domains.txt > domains.txt
awk -f segments.awk ecod.${ver}.domains.txt > segments.txt

# parse pdb files (takes forever, I've been storing the zips)
xargs -a segments.txt -L1 ./chopper.sh

# move to db directory
cd ../../db

# prepare database (will prompt for password)
psql -d rupee <<EOF
    truncate table ecod_domain;
    truncate table ecod_grams;
    truncate table ecod_hashes;
    \i x_ecod_domain.sql
EOF

## move to app directory
cd ../rupee-search/target

## import and hash
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i ECOD
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h ECOD


