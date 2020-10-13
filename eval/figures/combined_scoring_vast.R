
rm(list = ls())

source('scoring.R')

plot <- get_scoring_plot(
        'VAST',
        'RUPEE vs: VAST\nbenchmark: casp_vast_d199\nstructure database: PDB chains\nRUPEE search type: Full-Length', 
        'TM-score (avg)',
        'scoring_vast_fl.txt', 
        c('All','Top','Fast','VAST'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.3, 0.6)
)

ggsave('combined_scoring_vast.eps', plot = plot, width = 2.3, height = 2.5, dpi = 600)

