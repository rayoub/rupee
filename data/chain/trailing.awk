
BEGIN { 
    processed = 0
} 
/^ATOM/ { processed = 1 }
processed == 1 { print }

