#! /bin/bash

# run like this 'xargs -a segments.txt -L1 ./chopper.sh'
# do not use -P option since we are appending, i.e. >>, at end

# chops downloaded pdbs and copies to pdb directory

ecod_id=$1
seg_num=$2
pdb_id=$3
chain=$4
sres=$5
sins=$6
eres=$7
eins=$8

# exit if file doesn't exist
if [ -e "../chain/pdb/${pdb_id}${chain}.pdb.gz" ]; then
    dir="pdb"
elif [ -e "../chain/obsolete/${pdb_id}${chain}.pdb.gz" ]; then
    dir="obsolete" 
else 
    echo "Pdb file for ${ecod_id} doesn't exist."
    exit 1
fi

echo "Processing ${ecod_id}${seg_num}"

start=${sres#NA}${sins#NA}
end=${eres#NA}${eins#NA}

# 1. get ATOM and HETATM records only
# 2. stop processing at the end of the first model 
gunzip -c "../chain/${dir}/${pdb_id}${chain}.pdb.gz" | sed -rn -e '/^(ATOM|HETATM)/p' -e '/^ENDMDL/q' |

# 1. range pattern for the current range
# 2. get the end as a distinct rule since 1. is not greedy
awk -v start=$start -v end=$end -f chopper.awk | gzip -c >> ./pdb/${ecod_id}.pdb.gz


