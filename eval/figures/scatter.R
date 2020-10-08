
library(ggplot2)
library(plyr)

# read in data file
df <- read.csv('scatter.txt')

# *** plot
plot <- 

    ggplot(df, aes(rupee_score, mtm_score)) +
        
    # points
    geom_point(
        size = rel(0.50)
    ) + 

    # reference line
    geom_abline(
        slope = 1,
        intercept = 0,
        size = rel(0.25)
    ) +

    # scales
    scale_x_continuous(
        limits = c(0,1)
    ) + 
    scale_y_continuous(
        limits = c(0,1)
    ) +
    
    # labels
    xlab('RUPEE best scores') + 
    ylab('mTM-align best scores') +
        
    # title
    ggtitle('Best scores from RUPEE vs. mTM-align')

# *** theme
theme <-  

    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_text(size = 7),
        plot.margin = margin(5,15,0,5), 

        panel.grid = element_blank(),
        panel.spacing = unit(4,'mm'),

        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 7, margin = margin(0,10,0,0))
    ) 

ggsave('scatter.eps', plot = (plot + theme), width = 2.5, height = 2)

