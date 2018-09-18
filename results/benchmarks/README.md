
## Benchmarks 

Following is a list of the benchmarks used to evaluate the performance of RUPEE.

#### scop_d500

Identical to d500 used in the mTM paper.

#### scop_d499

Subset of d500 that is defined in SCOP 2.07. 
This excludes *d4pwvb_*, which is defined in SCOP 2.06 but not SCOP 2.07.

#### scop_d437

Subset of d499 that includes all domains for which mTM returns greater than or equal to 50 SCOP 2.07 domain results. 

#### scop_d360

Subset of d437 that includes all domains for which mTM returns greater than or equal to 100 SCOP 2.07 domain results. 


#### scop_d100

Subset of d360 including the first 100 domains. Used for development testing. 

#### scop_d50

Subset of d360 including the first 50 domains. Used for development testing. 

#### cath_diverse_family_d100

CATH superfamily representatives of the 100 most diverse CATH superfamilies in CATH v4.1

#### cath_diverse_family_d99

Subset of d100 that includes all domains for which CATHEDRAL returns greater than or equal to 50 results. 

#### cath_diverse_family_d94

Subset of d99 that includes all domains for which CATHEDRAL returns the query domain as the first domain. This helps ensure we are not evaluating RUPEE against possible CATHEDRAL bugs.   
