#! /bin/bash

comm -3 <(cut -d, -f2 $1) <(cut -d, -f2 $2)
