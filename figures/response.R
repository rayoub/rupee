
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data file
df <- read.csv('response.txt')

# reorder factor levels
df$app <- factor(df$app, levels = c('RUPEE', 'CATHEDRAL'))

ggplot(df, aes(residue_count, response_time, group = app, color = app)) +
    
    # geoms
    geom_line(
        size = rel(0.5)
    ) + 
    geom_point(
        size = rel(0.75)
    ) + 
    geom_vline(
        xintercept = 162,
        colour = 'grey50', 
        size = rel(0.2)
    ) +

    # scales
    scale_color_grey(
        start = 0,
        end = 0.6
    ) + 
    
    # guides
    guides(color = guide_legend(override.aes = list(size = rel(0.5)))) + 

    # axis labels
    labs(
         x = '# of residues', 
         y = 'response time'
    ) + 
    
    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_blank(), 
        plot.margin = margin(0,15,0,0), 

        panel.grid = element_blank(),
        
        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        
        legend.text = element_text(size = 7),
        legend.title = element_blank(), 
        legend.position = 'bottom',
        legend.margin = unit(0,'mm'),
        legend.direction = 'horizontal'
    ) 

ggsave('response.eps', width = 3, height = 2.0)


