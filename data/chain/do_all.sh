
ver=$1

# parse definition files
awk -f chains.awk chain_list_v07_18_2018.txt > chains.txt

# parse pdb files
xargs -a chains.txt -L1 ./chopper.sh

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
cd ../rupee-mgr/target

## import and hash
java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i CHAIN
java -jar rupee-mgr-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h CHAIN


