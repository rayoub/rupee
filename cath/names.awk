
BEGIN {
    OFS = "#"
}
$0 !~ /^#/ { 
    description_index = index($0,":")
    print $1, $2, substr($0, description_index + 1)
}


