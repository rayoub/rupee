#! /bin/bash

# run like this 'xargs -a eus.txt -L1 ./chopper.sh'
# do not use -P option since we are appending, i.e. >>, at end

# chops target preds into eus and copies to eu_preds directory

target=$1
eu=$2
sres=$3
eres=$4

for f in ./target_preds/${target}/* 
do
  
    base=$(basename -- $f)
    stripped=${base%_TS1}
    name=${target}-${eu}-${stripped}

    # ATOM records only
    cat $f | sed -rn -e '/^(ATOM)/p' |

    # chop EU and save
    awk -v sres=$sres -v eres=$eres -f chopper.awk > ./eu_preds/${name}.pdb

done




