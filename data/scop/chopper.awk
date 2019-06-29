
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
}  

# if start and end variable are empty we get whole chain
start == "" && end == "" { print; next }

# otherwise we get everything between start and end inclusive
gensub(/[[:space:]]/,"","g",$9) == start , gensub(/[[:space:]]/,"","g",$9) == end { print }
gensub(/[[:space:]]/,"","g",$9) == end { print } 

