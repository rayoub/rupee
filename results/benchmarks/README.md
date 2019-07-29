
## Benchmarks 

Following is a list of the benchmarks used to evaluate the performance of RUPEE.

### SCOP benchmarks

#### scop_d500

Identical to d500 used in the mTM paper.

#### scop_d499

Subset of scop_d500 that is defined in SCOP 2.07. 
This excludes *d4pwvb_*, which is defined in SCOP 2.06 but not SCOP 2.07.

#### scop_d360

Subset of scop_d499 that includes all domains for which mTM returns greater than or equal to 100 SCOP 2.07 domain results. 

#### scop_d204

Subset of scop_d500 that is defined in SCOP 1.73. 

#### scop_d193

Subset of scop_d204 that includes all domains for which SSM returns results.
In some cases, a domain may not have enough secondary structures for SSM to work with. 

#### scop_d62

Subset of scop_d193 that includes all domains for which SSM returns greater than or equal to 50 results.

### CATH benchmarks

#### cath_d100

CATH superfamily representatives of the 100 most diverse CATH superfamilies in CATH v4.2.0.

#### cath_d99

Subset of cath_d100 that includes all domains for which CATHEDRAL returns a response within 12 hours.

### CASP-derived benchmarks

#### casp_d150

Consist of selected predictions for all 30 single-range evaluation units from CASP12 free-modeling targets (FM).
Predictions were selected from the best per evaluation unit of the top 5 performers who submitted predictions for all regular targets. These top 5 groups are: BAKER-ROSETTASERVER, Zhang-Server, QUARK, RaptorX and MULTICOM-NOVEL. 

#### casp_mtm_d144

Subset of casp_d150 that includes all domains for which mTM returns greater than or equal to 10 results. 

#### casp_mtm_d34

Subset of casp_mtm_d144 that includes all domains for which mTM returns greater than or equal to 100 results. 

#### casp_cathedral_d149

Subset of casp_d150 that includes all domains for which CATHEDRAL returns greater than or equal to 100 results. 

#### casp_ssm_d149

Subset of casp_d150 that includes all domains for which SSM returns greater than or equal to 100 results. 



