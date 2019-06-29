
# exclude comments, genetic domains and artifacts
$0 !~ /^#/ && $1 !~ /d[0-9a-z]{4}(\.|_)/ && $4 !~ /l\.1\.1\.1/ {
   
    # get field values 
    scop_id = $1
    pdb_id = $2
    split($4, sccs, ".")
    class = sccs[1]
    fold = sccs[2]
    superfamily = sccs[3]
    family = sccs[4]
    sunid = $5

    print scop_id, pdb_id, sunid, class, fold, superfamily, family
}
