
# fed by: tail -n +3 *.txt | sed -e '/^ *$/d' -e '/=/d' | 

BEGIN {
    key = ""
}

$0 ~ /^[0-9]/ {
   
    # rule for key column 
    key = substr($1, 1, length($1)-1)
}

$0 !~ /^[0-9]/ {
   
    # rule for entry columns
    print key, $1, $2
}

