#! /bin/bash

for id in $(find ./rupee -name '*.results' -printf '%f\n'); do
    cat ./rupee/${id}
done;

