#! /bin/bash

# results directory
cd ~/git/rupee/results
rm -r rupee
rm rupee.results
./rupee_run.sh
./rupee_results.sh > rupee.results

