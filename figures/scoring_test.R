
source('scoring.R')

#plot <- get_scoring_plot(
#        'vs. mTM',
#        'scoring_mtm_d246.txt',
#        c('RUPEE All-Aligned','RUPEE Top-Aligned','mTM'), 
#        c(1, 100),
#        c(1,10,20,30,40,50,60,70,80,90,100)
#)
#plot <- get_scoring_plot(
#        'vs. SSM',
#        'scoring_ssm_ce.txt',
#        c('RUPEE All-Aligned','RUPEE Top-Aligned','SSM'), 
#        c(1, 100),
#        c(1,10,20,30,40,50,60,70,80,90,100),
#        c(0, 10) # switch up for rmsd and q_score
#)
plot <- get_scoring_plot(
        'vs. CATHEDRAL',
        'scoring_cathedral_ssap.txt',
        c('RUPEE All-Aligned','RUPEE Top-Aligned','CATHEDRAL'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0, 100) # switch up for rmsd and ssap_score
)

ggsave('scoring_test.eps', plot = plot, width = 7.5, height = 2.25, dpi = 300)

