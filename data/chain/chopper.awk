
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
    processed = 0
} 
$8 != chain && processed == 1 { exit }  # prevent greedy match to end of file in case of missing end points
$8 == chain { processed = 1; print }

