#! /bin/bash

j=1
for i in ~/git/rupee/data/casp/eu_preds/*.pdb; do
    fn=$(printf "x%03dX.pdb" "$j") 
    cp $i $fn
    let j=j+1
done
