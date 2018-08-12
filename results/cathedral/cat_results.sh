#! /bin/bash

dir=$1

for id in $(find ${dir} -name '*.results' -printf '%f\n'); do
    awk -v id=${id%.results} -e "BEGIN { OFS = \",\" } { print id, \$0 }" ${dir}/${id}
done;

