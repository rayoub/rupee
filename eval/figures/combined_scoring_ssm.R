
rm(list = ls())

source('scoring.R')

plot <- get_scoring_plot(
        'SSM',
        'vs: SSM\nbenchmark: casp_ssm_d248\nstructure database: SCOP v1.73\nRUPEE search type: Q-score',
        'Q-score',
        'scoring_ssm_q.txt', 
        c('All','Top','Fast','SSM'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.05, 0.30)
)

ggsave('combined_scoring_ssm.eps', plot = plot, width = 3, height = 2.75, dpi = 300)

