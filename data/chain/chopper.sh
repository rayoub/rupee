#! /bin/bash

# run like this 'xargs -a chains.txt -L1 -P8 ./chopper.sh'

# chops downloaded pdbs and copies to pdb directory

# create output directory
[ -d ./pdb ] || mkdir ./pdb

pdb_id=$1
chain_id=$2

# exit if file doesn't exist
if [ -e "../pdb/pdb/pdb${pdb_id}.ent.gz" ]; then
    dir="pdb"
elif [ -e "../pdb/obsolete/pdb${pdb_id}.ent.gz" ]; then 
    dir="obsolete"
else
    echo "Pdb file for ${chain_id} doesn't exist."
    exit 1
fi

echo "Processing ${pdb_id}${chain_id}"

# 1. get ATOM and HETATM records only
# 2. stop processing at the end of the first model 
gunzip -c "../pdb/${dir}/pdb${pdb_id}.ent.gz" | sed -rn -e '/^(ATOM|HETATM)/p' -e '/^ENDMDL/q' |

# 1. range pattern for the current range
# 2. get the end as a distinct rule since 1. is not greedy
# 3. CA should follow N within a residue
awk -v chain=${chain_id} -f chopper.awk | gzip -c > ./pdb/${pdb_id}${chain_id}.pdb.gz


