
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
    processed = 0
}  

# prevent greedy match to end of file in case of missing end points
$8 != chain && processed == 1 { exit } 

# if start and end variable are empty we get whole chain
$8 == chain && start == "" && end == "" { processed = 1; print; next }

# otherwise we get everything between start and end inclusive
$8 == chain && gensub(/[[:space:]]/,"","g",$9) == start , $8 == chain && gensub(/[[:space:]]/,"","g",$9) == end { processed = 1; print }
$8 == chain && gensub(/[[:space:]]/,"","g",$9) == end { print } 

