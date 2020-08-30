#! /bin/bash

# bundle definitions
./bundle.sh > bundle.txt

# delete output directory if it already exist
[ -d ./chopped ] && rm -r chopped

# create output directory
mkdir ./chopped

# run the chopper
xargs -a bundle.txt -P8 -L1 ./chopper.sh

# gzip the chopped files 
find ./chopped -name "*.pdb" -exec gzip {} \;


