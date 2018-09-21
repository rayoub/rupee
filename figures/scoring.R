
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data file
df <- read.csv('scoring.txt')
refs <- read.csv('scoring_refs.txt')

# reorder factor levels 
df$app <- factor(df$app, levels = c('RUPEE', 'RUPEE Fast', 'mTM'))

ggplot(df, aes(n, avg_cume_score, group = interaction(app, score_type), color = app)) +
    
    # geoms
    geom_hline(
        data = refs,
        colour = 'grey50', 
        size = rel(0.2),
        aes(yintercept = yintercept)
    ) +
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

    # faceting
    facet_wrap(~score_type, ncol = 1, scales = 'free_y') + 
    
    # guides
    guides(color = guide_legend(override.aes = list(size = rel(0.5)))) + 

    # axis labels
    labs(
         x = 'rank', 
         y = 'average cumulative value'
    ) + 
    
    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_blank(), 
        plot.margin = margin(0,15,0,0), 

        panel.grid = element_blank(),
        panel.margin.x = unit(4,'mm'),
        
        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        
        legend.text = element_text(size = 7),
        legend.title = element_blank(), 
        legend.position = 'bottom',
        legend.margin = unit(0,'mm'),
        legend.box = 'horizontal',
        legend.direction = 'horizontal',

        strip.background = element_blank(),
        strip.text.x = element_text(color = 'black')
    ) 

ggsave('scoring.pdf', width = 3, height = 4.25)


