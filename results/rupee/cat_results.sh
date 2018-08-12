#! /bin/bash

dir=$1

for id in $(find ${dir} -name '*.txt' -printf '%f\n'); do
    cat ${dir}/${id}
done;

