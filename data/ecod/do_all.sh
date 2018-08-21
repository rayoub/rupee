
ver=$1

# parse definition files
awk -f domains.awk ecod.develop210.domains.txt > domains.txt
awk -f segments.awk ecod.develop210.domains.txt > segments.txt

# parse pdb files
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
cd ../rupee-mgr/target

## import and hash
java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i ECOD
java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h ECOD


