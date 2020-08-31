#! /bin/bash

ver=$1

# bundle definitions
./bundle.sh > bundle.txt

# delete output directory if it already exist
[ -d ./chopped ] && rm -r chopped

# create output directory
mkdir ./chopped

# message
echo "To the chopper! This is going to take about 45 minutes"

# run the chopper
xargs -a bundle.txt -P8 -L1 ./chopper.sh

# delete empty chopped files
find ./chopped -size 0 -delete

# message
echo "Zipping the chopped files: This is going to take about 15 minutes"

# gzip the chopped files 
find ./chopped -name "*.ent" -exec gzip {} \;

# move to app directory
cd ../../rupee-search/target

# chop the bundle files
java -jar rupee-search-0.0.1-SNAPSHOT-jar-with-dependencies.jar -b $ver 

# done
echo "Done"

