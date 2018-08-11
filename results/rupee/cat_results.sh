#! /bin/bash

dir=$1

for id in $(find ${dir} -name '*.results' -printf '%f\n'); do
    cat ${dir}/${id}
done;

