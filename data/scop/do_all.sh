
cla=$1
des=$2

# parse definition files
awk -f domains.awk $cla | sort > domains.txt
awk -f segments.awk $cla | sort > segments.txt
awk -f names.awk $des > names.txt

# delete output directory if it already exist
[ -d ./pdb ] && rm -r pdb

# create output directory
mkdir ./pdb

# parse pdb files (takes forever, I've been storing the zips)
xargs -a segments.txt -L1 ./chopper.sh

# move to db directory
cd ../../db

# prepare database (will prompt for password)
psql -d rupee <<EOF
    truncate table scop_name;
    truncate table scop_domain;
    truncate table scop_grams;
    truncate table scop_hashes;
    \i x_scop_name.sql
    \i x_scop_domain.sql
EOF

# move to app directory
cd ../rupee-search/target

# import and hash
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i SCOP
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h SCOP


