
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data files
df <- read.csv('response_rupee.txt')
    
# reorder factor levels 
df$app <- factor(df$app, levels = c('RUPEE Top-Aligned', 'RUPEE Test', 'mTM', 'SSM'))

# *** plot

plot <- 

    ggplot(df, aes(gram_count, timing, group = app, color = app)) +
        
    # geoms
    geom_point(
        size = rel(0.5)
    ) + 
    geom_smooth(     
        size = rel(0.5),
        show.legend = FALSE
    ) + 

    # scales
    scale_color_manual(
        values = c('#e41a1c', '#4daf4a', '#984ea3', '#ff7f00'),
        labels = c('RUPEE Top-Aligned', 'RUPEE Test', 'mTM', 'SSM')
    ) + 
    scale_y_continuous(
        trans = "log10",
        limits = c(1,10000)
    ) + 

    # guides
    guides(color = guide_legend(override.aes = list(size = rel(1)))) + 

    # axis labels
    labs(
         x = 'residue count', 
         y = 'response time (seconds)'
    ) + 
    
    # title
    ggtitle('Response Times')

# *** theme

theme <-  

    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_text(size = 8, hjust = 0.5),
        plot.margin = margin(5,15,0,5), 

        panel.grid = element_blank(),
        panel.spacing = unit(4,'mm'),

        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        
        legend.text = element_text(size = 7),
        legend.title = element_blank(), 
        legend.position = 'bottom',
        legend.direction = 'horizontal',
        legend.spacing = unit(0,'mm')
    ) 

    theme <- theme + theme(axis.title.y = element_text(size = 8))

# *** combined

combined <- plot + theme

ggsave('response_rupee.eps', plot = combined)



