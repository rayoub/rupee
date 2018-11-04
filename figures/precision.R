
# variable
# 1. factor levels
# 2. x scale 

library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data file
df <- read.csv('precision.txt')

# reorder factor levels
df$app <- factor(df$app, levels = c('RUPEE', 'RUPEE Fast', 'mTM'))
    
ggplot(df, aes(n, level_precision, group = app, color = app, linetype = app)) +

    # geoms
    geom_line(
        size = rel(0.5)
    ) + 

    # scales
    scale_color_manual(
        values = c("#e41a1c", "#377eb8", "#4daf4a")
    ) + 
    scale_linetype_manual(
        values = c("solid","dashed","dotted")
    ) + 
    scale_x_continuous(
        limits = c(1, 100),
        breaks = c(1, seq(10, 100, by = 10))
        #limits = c(1, 50),
        #breaks = c(1, seq(5, 50, by = 5))
    ) + 
    
    # guides
    guides(color = guide_legend(override.aes = list(size = rel(0.5)))) + 

    # axis labels
    labs(
         x = 'rank', 
         y = 'average precision'
    ) + 
    
    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_text(size = 8),
        plot.margin = margin(0,15,0,0), 

        panel.grid = element_blank(),
        
        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        
        legend.text = element_text(size = 7),
        legend.title = element_blank(), 
        legend.position = 'bottom',
        legend.direction = 'horizontal',
        legend.margin = unit(0,'mm')
    ) 

ggsave('precision.eps', width = 3, height = 2.5)


