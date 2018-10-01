
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data files
df <- read.csv('torsion.txt')
refs <- read.csv('torsion_refs.txt')
annotes <- read.csv('torsion_annotes.txt')

# map values
df$sse <- mapvalues(df$sse, from = c('Turn', 'Bridge', 'Bend', 'Coil'), to = c('Helix', 'Strand', 'Bend/Coil', 'Bend/Coil'))

# subset data
df <- subset(df, df$sse == 'Helix' | df$sse == 'Strand' | df$sse == 'Bend/Coil')

# reorder factor levels
df$sse <- factor(df$sse, levels = c('Helix', 'Strand', 'Bend/Coil'))

# wrap and translate torsion angles
df$phi <- ifelse(df$phi < 0, df$phi + 360, df$phi)
df$phi <- df$phi + 200

# color scale
color_scale <- c('Helix' = 'red', 'Strand' = 'green', 'Bend/Coil' = 'black')

ggplot(df, aes(phi, psi, color = sse)) +
    
    # geoms
    geom_point(
        size = rel(0.01)
    ) + 
    geom_vline(
        xintercept = seq(200, 560, by = 90), 
        colour = 'grey50', 
        size = rel(0.2)
    ) +
    geom_hline(
        yintercept = seq(-180, 180, by = 30), 
        colour = 'grey50', 
        size = rel(0.2)
    ) +
    geom_segment(
        data = refs,
        size = rel(0.4),
        lineend = 'square',
        aes(x, y, xend = xend, yend = yend),
        inherit.aes = FALSE
    ) + 
    geom_text(
        data = annotes, 
        size = 3, 
        color = 'black',
        aes(x = phi, y = psi, label = label)
    ) +
    
    # scales
    scale_x_continuous(
        limits = c(0,560),
        expand = c(0,0),
        breaks = c(200,290,380,470,560),
        labels = c('0\u00B0','90\u00B0','\u00B1180\u00B0','-90\u00B0','0\u00B0')
    ) + 
    scale_y_continuous(
        limits = c(-180,180),
        expand = c(0,0),
        breaks = c(-150,-120,-90,-60,-30,0,30,60,90,120,150,180),
        labels = c('-150\u00B0','-120\u00B0','-90\u00B0','-60\u00B0','-30\u00B0','0\u00B0','30\u00B0','60\u00B0','90\u00B0','120\u00B0','150\u00B0','\u00B1180\u00B0')
    ) + 
    scale_color_manual(NULL, values = color_scale) + 
    
    # guides
    guides(color = guide_legend(override.aes = list(size = rel(1)))) + 

    # ceordinates 
    coord_polar(
       theta = 'y',
       direction = -1
    ) +

    # faceting
    facet_wrap(~sse, nrow = 1, scales = 'fixed') + 

    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_blank(), 
        plot.margin = margin(0,0,0,0),

        panel.border = element_blank(),
        panel.grid = element_blank(),
        panel.spacing.x = unit(0,'pt'),
        
        axis.text = element_blank(), 
        axis.text.y = element_blank(),
        axis.ticks = element_blank(),
        axis.title = element_blank(),
        axis.line = element_blank(),

        strip.background = element_blank(),
        strip.text = element_blank(),
        
        legend.title = element_blank(), 
        legend.text = element_text(size = 7),
        legend.position = 'bottom',
        legend.margin = margin(0,0,0,0),
        legend.direction = 'horizontal'
    )

ggsave('torsion_refs.eps', width = 7, height = 2.5)

