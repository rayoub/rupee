
BEGIN {
    seglen = 6
}
$0 !~ /^#/ { 

    doms = $2
    frags = $3 

    sub(/D0?/, "", doms)
    sub(/F0?/, "", frags)
    
    # convert to numeric 
    doms = doms+0
    frags = frags+0
    parts = doms + frags
   
    # point to first segment 
    ptr = 4 

    # segments in first domain
    fsegs = $ptr

    # iterate
    for(i = 1; i <= doms; i++) {
        segs = $ptr
        for(j = 1; j <= segs; j++) {
            chain = $(ptr + 1)
            sres = $(ptr + 2)
            sins = $(ptr + 3)
            eres = $(ptr + 5)
            eins = $(ptr + 6)
            sub(/\-/,"NA",sins)
            sub(/\-/,"NA",eins)
            if(parts > 1 || fsegs > 1) {
                dom = sprintf("%02d", i)
            }
            else {
                dom = "00" 
            }
            if(segs > 1){
                print $1 dom, j, substr($1,1,4), chain, sres, sins, eres, eins
            }
            else {
                print $1 dom, 1, substr($1,1,4), chain, sres, sins, eres, eins
            }
            ptr += seglen
        }
        ptr++
    }
}


