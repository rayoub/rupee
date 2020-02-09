#! /bin/bash

# run like this 'xargs -a eus_*.txt -L1 ./chopper.sh <group_id>'

# chops target preds into eus and copies to eu_preds directory

grp=$1
target=$2
eu=$3
sres=$4
eres=$5

for f in ./target_preds/${target}/*TS${grp}*
do

    # in the event of null glob
    [ -f "$f" ] || continue

    base=$(basename -- $f)
    stripped=${base%_1}
    name=${stripped}-${eu}

    # ATOM records only
    cat $f | sed -rn -e '/^(ATOM)/p' |

    # chop EU and save
    awk -v sres=$sres -v eres=$eres -f chopper.awk > ./eu_preds/${name}.pdb

done




