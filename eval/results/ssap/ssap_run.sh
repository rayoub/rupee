#! /bin/bash

xargs -a ssap_input.txt -L1 -P8 ./cath-ssap --pdb-path /home/ayoub/git/rupee/data/cath/pdb/ --min-score-for-files 101

