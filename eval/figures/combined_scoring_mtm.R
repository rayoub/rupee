
rm(list = ls())

source('scoring.R')

plot <- get_scoring_plot(
        'mTM-align',
        'RUPEE vs: mTM-align\nbenchmark: casp_d250\nstructure database: PDB chains\nRUPEE search type: Contained-In',
        'TM-score (q)',
        'scoring_mtm_ci.txt',
        c('All','Top','Fast','mTM-align'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.10, 0.65)
)

ggsave('combined_scoring_mtm.eps', plot = plot, width = 2.3, height = 2.5, dpi = 300)

