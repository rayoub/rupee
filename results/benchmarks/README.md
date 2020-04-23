
## Benchmarks from the 2019 PLoS ONE paper and hopefully a future paper.

### SCOP benchmarks

#### scop_d500

Identical to d500 used in the mTM paper.

#### scop_d499

Subset of scop_d500 that is defined in SCOP 2.07. 
This excludes *d4pwvb_*, which is defined in SCOP 2.06 but not SCOP 2.07.

#### scop_d360

Subset of scop_d499 that includes all domains for which mTM returns greater than or equal to 100 SCOP 2.07 domain results. 
In the PLoS ONE 2019 paper we eliminated 139 benchmark structures on behalf of mTM, which in retrospect, was unfair to RUPEE.

#### scop_d204

Subset of scop_d500 that is defined in SCOP 1.73. 

#### scop_d193

Subset of scop_d204 that includes all domains for which SSM returns results.
In some cases, a domain may not have enough secondary structures for SSM to work with. 

#### scop_d62

Subset of scop_d193 that includes all domains for which SSM returns greater than or equal to 50 results.
In the PLoS ONE 2019 paper we eliminated 131 benchmark structures on behalf of SSM, which in retrospect, was unfair to RUPEE.

### CATH benchmarks

#### cath_d100

CATH superfamily representatives of the 100 most diverse CATH superfamilies in CATH v4.2.0.

#### cath_d99

Subset of cath_d100 that includes all domains for which CATHEDRAL returns a response within 12 hours.

## Benchmarks for a paper currently under development. 

### CASP-derived benchmarks

#### casp_d250

Consist of all model 1 predictions submitted by 10 selected groups for all 25 single-range evaluation units for CASP13 free-modeling (FM) target domains.
The selected groups consist of the top 10 groups ranked by the Assessors' formula (GDT TS + QCS) applied to free-modeling targets.  

#### casp_mtm_d246

Subset of casp_d250 that includes all domains for which mTM returned results. 
For all benchmark structures, mTM returned 10 or more results. 

#### casp_ssm_q_d240

Subset of casp_d250 that includes all domains for which SSM, based on Q-score, returned 100 or more results.

#### casp_ssm_rmsd_d233

Subset of casp_d250 that includes all domains for which SSM, based on RMSD, returned 100 or more results.

#### casp_cathedral_d247

Subset of casp_d250 that includes all domains for which CATHEDRAL returned 100 or more results.

#### casp_vast_d199

Subset of casp_d250 that includes all domains for which VAST returned 100 or more results.


