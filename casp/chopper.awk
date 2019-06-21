
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
}  

# otherwise we get everything between start and end inclusive
gensub(/[[:space:]]/,"","g",$9) == sres , gensub(/[[:space:]]/,"","g",$9) == eres { print }
gensub(/[[:space:]]/,"","g",$9) == eres { print } 

