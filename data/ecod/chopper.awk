
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
} 
gensub(/[[:space:]]/,"","g",$9) == start , gensub(/[[:space:]]/,"","g",$9) == end { print }
gensub(/[[:space:]]/,"","g",$9) == end { print } 

