
# delete output directory if it already exist
[ -d ./chopped ] && rm -r chopped

# create output directory
mkdir ./chopped

# run the chopper
xargs -a bundle.txt -P8 -L1 ./chopper.sh


