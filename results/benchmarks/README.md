
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
In the PLOS ONE 2019 paper we eliminated 139 benchmark structures on behalf of mTM, which in retrospect, was unfair to RUPEE.

#### scop_d204

Subset of scop_d500 that is defined in SCOP 1.73. 

#### scop_d193

Subset of scop_d204 that includes all domains for which SSM returns results.
In some cases, a domain may not have enough secondary structures for SSM to work with. 

#### scop_d62

Subset of scop_d193 that includes all domains for which SSM returns greater than or equal to 50 results.
In the PLOS ONE 2019 paper we eliminated 131 benchmark structures on behalf of SSM, which in retrospect, was unfair to RUPEE.

### CATH benchmarks

#### cath_d100

CATH superfamily representatives of the 100 most diverse CATH superfamilies in CATH v4.2.0.

#### cath_d99

Subset of cath_d100 that includes all domains for which CATHEDRAL returns a response within 12 hours.

### CASP-derived benchmarks

#### casp_d250

Consist of all model 1 predictions submitted by 10 selected groups for all 25 single-range evaluation units for CASP13 free-modeling (FM) targets.
The selected groups consist of the top 10 ranked groups by the Assessors' formula (GDT TS + QCS) applied to free-modeling targets.  

#### casp_mtm_d246

Subset of casp_d250 that includes all domains for which mTM returned results. 
For all benchmark structure, mTM returned 10 or more results. 

#### casp_cathedral_d247

Subset of casp_d250 that includes all domains for which CATHEDRAL returned results.
For all benchmark structure, CATHEDRAL returned 100 or more results. 



