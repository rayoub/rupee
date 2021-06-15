
BEGIN { 
    OFS = "#"
    FPAT = "([^[:space:]]+)|(\"[^\"]+\")"
    REAL_NF = 14
}
$0 !~ /^#/ {

        diff = NF - REAL_NF
     
        ecod_id = $2

        split($4, xhtf, ".")
        x = xhtf[1]
        h = xhtf[2]
        t = xhtf[3]
        f = xhtf[4]

        pdb_id = $5

        arch_name = $9
        for (i = 10; i < 10 + diff - 1; i++) {
            arch_name = arch_name " " $(i)
        }

        x_name = $(9 + diff)
        h_name = $(10 + diff)
        t_name = $(11 + diff)
        f_name = $(12 + diff)

        gsub(/["]/,"",x_name)
        gsub(/["]/,"",h_name)
        gsub(/["]/,"",t_name)
        gsub(/["]/,"",f_name)

        print ecod_id, pdb_id, x, h, t, f, arch_name, x_name, h_name, t_name, f_name
    }
