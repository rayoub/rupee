
BEGIN {
    OFS = "#"
}
$2 ~ /cl|cf|sf|fa/ { 
    description_index = index($0,"-")
    description = substr($0, description_index + 1)
    gsub(/^\s+/, "", description)
    print $3, description
}
