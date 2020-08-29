#! /bin/bash

# used to create bundle.txt

tail -n +3 bundles/*.txt | sed -e '/^ *$/d' -e '/=/d' | awk -f bundle.awk 



