#! /bin/bash

# bundle definitions
./bundle.sh > bundle.txt

# delete output directory if it already exist
[ -d ./chopped ] && rm -r chopped

# create output directory
mkdir ./chopped

# run the chopper
xargs -a bundle.txt -P8 -L1 ./chopper.sh

# delete empty chopped files
find ./chopped -size 0 -delete

# gzip the chopped files 
find ./chopped -name "*.ent" -exec gzip {} \;


