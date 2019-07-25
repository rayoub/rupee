
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data files
df <- read.csv('scoring_rupee_scop_d360.txt')
refs <- read.csv('scoring_refs.txt')

# reorder factor levels 
df$app <- factor(df$app, levels = c('RUPEE All-Aligned', 'RUPEE Top-Aligned'))

# *** plot

plot <- 
    
    ggplot(df, aes(n, avg_cume_score, group = interaction(app), color = app, linetype = app)) +
    
    # geoms
    geom_line(
        size = rel(0.5)
    ) + 

    # scales        
    scale_color_manual(
        values = c("#e41a1c", "#377eb8"),
        labels = c("RUPEE All-Aligned","RUPEE Top-Aligned")
    ) + 
    scale_linetype_manual(
        values = c("solid", "dashed"),
        labels = c("RUPEE All-Aligned","RUPEE Top-Aligned")
    ) + 
    scale_x_continuous(
        limits = c(1,100),
        breaks = c(1, seq(10, 100, by = 10))
    ) + 

    # guides
    guides(linetype = guide_legend(override.aes = list(size = rel(0.5)))) + 

    # axis labels
    labs(
         x = 'rank', 
         y = 'value'
    ) +

    # title
    ggtitle('RUPEE All-Aligned vs. RUPEE Top-Aligned')

# *** theme

theme <-  

    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_text(size = 8, hjust = 0.5),
        plot.margin = margin(5,15,0,0), 

        panel.grid = element_blank(),
        panel.spacing = unit(4,'mm'),
        
        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        axis.title.y = element_blank(),
        
        legend.text = element_text(size = 7),
        legend.title = element_blank(), 
        legend.position = 'bottom',
        legend.direction = 'horizontal',
        legend.spacing = unit(0,'mm'),
    ) 

    theme <- theme + theme(strip.text = element_text(size = 8))

# *** combined

combined <- plot + theme

ggsave('scoring_rupee_scop_d360.eps', plot = combined)

