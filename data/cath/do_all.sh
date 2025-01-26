#! /bin/bash

# do not parallelize this script due to appending at the bottom
ver=$1

# delete output directory if it already exist
[ -d ./pdb ] && rm -r pdb

# create output directory
mkdir ./pdb

# parse definition files
awk -f domains.awk cath-domain-list-${ver}.txt > domains.txt
awk -f segments.awk cath-domain-boundaries-${ver}.txt > segments.txt
awk -f names.awk cath-names-${ver}.txt > names.txt

# DO NOT PARALLELIZE THIS CALL 
# parse pdb files: (takes forever, like a day and a half)
xargs -a segments.txt -L1 ./chopper.sh

# move to db directory
cd ../../db

# prepare database (will prompt for password)
psql -d rupee -U postgres <<EOF
    truncate table cath_name;
    truncate table cath_domain;
    truncate table cath_grams;
    truncate table cath_hashes;
    \i x_cath_name.sql
    \i x_cath_domain.sql
EOF

# move to app directory
cd ../rupee-search/target

# import and hash
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i CATH
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h CATH

# done
echo
echo "Done"
