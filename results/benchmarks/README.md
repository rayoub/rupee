
## Benchmarks 

Following is a list of the benchmarks used to evaluate the performance of RUPEE.

### Against mTM

Currently, mTM appears to be working with a combined set of domains from SCOP 2.06 and SCOP 2.07. 
It is for this reason care has been taken to filter out domains defined in SCOP 2.06, but not in SCOP 2.07.
For instance, a search for *d4pwvb_* will return both *d4pwvb_* from SCOP 2.06 and *d4pwvb1* from SCOP 2.07. 
This only occurs for a few domains that have been modified from SCOP 2.06 to SCOP 2.07. 
Nevertheless, this has to be accounted for to achieve a fair comparison. 

#### scop_d500

Identical to d500 used in the mTM paper.

#### scop_d499

Subset of d500 that is defined in SCOP 2.07. 
This excludes *d4pwvb_*, which is defined in SCOP 2.06 but not SCOP 2.07.

#### scop_d437

Subset of d499 that includes all domains for which mTM returns greater than or equal to 50 SCOP 2.07 domain results. 

#### scop_d360

Subset of d437 that includes all domains for which mTM returns greater than or equal to 100 SCOP 2.07 domain results. 

### Against CATHEDRAL

#### cath_d100

CATH superfamily representatives of the 100 most diverse CATH superfamilies in CATH v4.2.

#### cath_d99

Subset of d100 that includes all domains for which CATHEDRAL returns a response within 12 hours.

### Against SSM

#### scop_d204

Subset of d500 that is defined in SCOP 1.73. 

#### scop_d193

Subset of d204 that includes all domains for which SSM returns results.
In some cases, a domain may not have enough secondary structures for SSM to work with. 

#### scop_d62

Subset of d193 that includes all domains for which SSM returns greater than or equal to 50 results.
