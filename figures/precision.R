
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data file
df <- read.csv('precision.txt')

# reorder factor levels
df$app <- factor(df$app, levels = c('RUPEE', 'RUPEE Fast', 'CATHEDRAL'))

ggplot(df, aes(n, level_precision, group = interaction(app, hierarchy_level, lex.order = TRUE), color = app, linetype = hierarchy_level)) +
    
    # geoms
    geom_line(
        size = rel(0.5)
    ) + 

    # scales
    scale_color_grey(
        start = 0,
        end = 0.6
    ) + 
    scale_x_continuous(
        limits = c(1, 50),
        breaks = c(1, seq(5, 50, by = 5))
    ) + 
    
    # guides
    guides(color = guide_legend(override.aes = list(size = rel(0.5))), linetype = guide_legend(override.aes = list(size = rel(0.5)))) + 

    # axis labels
    labs(
         x = 'rank', 
         y = 'average precision'
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
        legend.box = 'horizontal',
        legend.direction = 'vertical'
    ) 

ggsave('precision.pdf', width = 3, height = 2.5)


