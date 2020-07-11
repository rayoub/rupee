
library(ggplot2)
library(plyr)

get_torsion_plot <- function () {

# clear environment
rm(list = ls())

# read in data files
df <- read.csv('torsion.txt')

# map values
df$sse <- mapvalues(df$sse, from = c('Turn', 'Bridge', 'Bend', 'Coil'), to = c('Helix', 'Strand', 'Bend/Coil', 'Bend/Coil'))

# reorder factor levels
df$sse <- factor(df$sse, levels = c('Helix', 'Strand', 'Bend/Coil'))

# color scale
color_scale <- c('Helix' = 'red', 'Strand' = 'green', 'Bend/Coil' = 'black')

# wrap and translate torsion angles
df$phi <- ifelse(df$phi < 0, df$phi + 360, df$phi)
df$phi <- df$phi + 200

torsion_plot <- ggplot(df, aes(phi, psi, color = sse)) +
    
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

    # coordinates 
    coord_polar(
       theta = 'y',
       direction = -1
    ) +

    # axis labels
    labs(
         x = expression(phi), 
         y = expression(psi)
    ) + 

    # default theme 
    theme_bw() +

    # default override
    theme(
        plot.title = element_blank(), 
        plot.margin = margin(5,0,0,0),

        panel.border = element_blank(),
        panel.grid = element_blank(),
        
        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 8), 
        axis.line = element_blank(),
        
        legend.title = element_blank(), 
        legend.text = element_text(size = 7),
        legend.position = 'bottom',
        legend.margin = margin(0,0,0,0),
        legend.direction = 'horizontal'
    )
}

