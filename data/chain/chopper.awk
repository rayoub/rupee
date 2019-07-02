
BEGIN { 
    FIELDWIDTHS  = "6 5 1 4 1 3 1 1 5" 
    processed = 0
} 
$8 != chain && processed == 1 { exit }  # stop processing if intervening chains
$8 == chain { processed = 1; print }

