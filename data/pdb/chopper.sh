#! /bin/bash

# run like this 'xargs -a bundle.txt -P8 -L1 ./chopper.sh'

# chops bundled chain and copies it to the chopped directory 

file=$1
chain1=$2
chain2=$3

pdb=${file:0:4}

# exit if file doesn't exist
if [ ! -e "./bundles/$file" ]; then
    echo "Bundle file for pdb $pdb chain $chain2 doesn't exist."
    exit 1
fi

echo "Processing bundle file for pdb $pdb chain $chain2" 

# 1. get ATOM and HETATM records only
# 2. stop processing at the end of the first model 
cat "./bundles/$file" | sed -rn -e '/^(ATOM|HETATM)/p' -e '/^ENDMDL/q' |

# get records corresponding to the chain
awk -v chain=${chain1} -f chopper.awk | 

# remove trailing HETATM records and save
tac | awk -f trailing.awk | tac > ./chopped/pdb${pdb}${chain2}.ent


