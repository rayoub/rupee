#! /bin/bash

xargs -a ssap_input.txt -L1 ./cath-ssap --slow-ssap-only --pdb-path /home/ayoub/git/rupee/data/cath/pdb/ --min-score-for-files 101

