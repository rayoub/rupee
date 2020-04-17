
source('scoring.R')

#plot <- get_scoring_plot(
#        'vs. mTM',
#        'scoring_mtm_ci.txt', # _rmsd, _ci
#        c('RUPEE All-Aligned','RUPEE Top-Aligned','mTM'), 
#        c(1, 100),
#        c(1,10,20,30,40,50,60,70,80,90,100),
#        c(0.20, 0.80), # c(0, 5) or c(0.20, 0.80)
#        FALSE
#)
#plot <- get_scoring_plot(
#        'vs. SSM',
#        'scoring_ssm_q.txt', # _ce, _fatcat, _fl, _q
#        c('RUPEE All-Aligned','RUPEE Top-Aligned','SSM'), 
#        c(1, 100),
#        c(1,10,20,30,40,50,60,70,80,90,100),
#        c(0.05, 0.25), # c(0, 6), c(0.25, 0.55) or c(0.05, 0.25)
#        FALSE
#)
#plot <- get_scoring_plot(
#        'vs. CATHEDRAL',
#        'scoring_cathedral_fl.txt', # _ce, _fatcat, _fl, _ssap
#        c('RUPEE All-Aligned','RUPEE Top-Aligned','CATHEDRAL'), 
#        c(1, 100),
#        c(1,10,20,30,40,50,60,70,80,90,100),
#        c(0.3, 0.55), # c(0, 5), c(0.30, 0.55) or c(60, 75)
#        FALSE
#)
plot1 <- get_scoring_plot(
        'vs. VAST',
        'scoring_vast_fl.txt', # _ce, _fatcat, _fl
        c('RUPEE All-Aligned','RUPEE Top-Aligned','VAST'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.1, 0.6), # c(0, 5) or c(0.1, 0.6)
        TRUE
)

ggsave('scoring_test.eps', plot = plot, width = 3.25, height = 3.25, dpi = 300)

