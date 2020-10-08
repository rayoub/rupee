
library(ggplot2)
library(plyr)

# read in data file
df <- read.csv('families.txt')

# *** plot
plot <- 

    ggplot(df, aes(superfamily_count, target_count)) +
        
    # points
    geom_col(
        size = rel(0.50)
    ) + 

    # scales
    scale_x_continuous(
        limits = c(1,10),
        breaks = c(1,2,3,4,5,6,7,8,9,10)
    ) + 
    
    # labels
    xlab('target count') + 
    ylab('superfamily count') +
        
    # title
    ggtitle('Superfamily counts per target')

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

ggsave('families.eps', plot = (plot + theme), width = 2.5, height = 2)

