
BEGIN {
    seglen = 6
}
# exclude comments, genetic domains and artifacts
$0 !~ /^#/ && $1 !~ /d[0-9a-z]{4}(\.|_)/ && $4 !~ /l\.1\.1\.1/ {

    # get id field
    scop_id = $1
    pdb_id = $2

    # iterate segments
    segs_len = split($3, segs, ",")
    for(i = 1; i <= segs_len; i++) {
       
        # chain and range
        split(segs[i],chain_range,":")
        chain = chain_range[1]
        range = chain_range[2]

        if (range == "") {
            negative = "false"
            start_res = "NA"
            start_ins = "NA"
            end_res = "NA"
            end_ins = "NA"
        }
        else {

            negative = "false"
            if (index(range, "-") == 1) {
                negative = "true"
                range = substr(range, 2)
            }
            
            # start and end
            split(range,start_end,"-")
            start = start_end[1]
            end = start_end[2]
           
            # res and ins 
            start_res = start+0
            end_res = end+0

            start_ins = "NA"
            if (length(start_res) != length(start)) 
                start_ins = substr(start, length(start_res) + 1)

            end_ins = "NA"
            if (length(end_res) != length(end)) 
                end_ins = substr(end, length(end_res) + 1)
        } 

        print scop_id, i, pdb_id, chain, (negative == "true"?"-":"") start_res, start_ins, end_res, end_ins
    }
}


