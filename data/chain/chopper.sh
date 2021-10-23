#! /bin/bash

# run like this 'xargs -a chains.txt -L1 -P8 ./chopper.sh'

# chops downloaded pdbs and copies to pdb directory

dir=$1
chain_id=$2
pdb_id=$3
chain_name=$4
residue_count=$5

# exit if file doesn't exist
if [ ! -e "../pdb/${dir}/pdb${pdb_id}.ent.gz" ]; then
    echo "Pdb file for ${chain_id} doesn't exist."
    exit 1
fi

echo "Processing ${chain_id}"

# 1. get ATOM and HETATM records only
# 2. stop processing at the end of the first model 
gunzip -c "../pdb/${dir}/pdb${pdb_id}.ent.gz" | sed -rn -e '/^(ATOM|HETATM)/p' -e '/^ENDMDL/q' |

# get records corresponding to the chain
awk -v chain=${chain_name} -f chopper.awk | 

# remove trailing HETATM records and save
tac | awk -f trailing.awk | tac | gzip -c > ./${dir}/${chain_id}.pdb.gz


