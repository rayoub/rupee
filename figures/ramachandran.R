
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data file
df <- read.csv('torsion.txt')

# reorder factor levels
df$sse <- factor(df$sse, levels = c('Helix', 'Turn', 'Strand', 'Bridge', 'Bend', 'Coil'))

ggplot(df, aes(phi, psi)) +
    
    # geoms
    geom_point(
        size = rel(0.01)
    ) + 

    # coordinates
    coord_fixed(
       ratio = 1
    ) +
    
    # scales
    scale_x_continuous(
        limits = c(-180,180),
        expand = c(0,0),
        breaks = c(-180,-90,0,90,180),
        labels = c('-180\u00B0','-90\u00B0','0\u00B0','90\u00B0','180\u00B0')
    ) + 
    scale_y_continuous(
        limits = c(-180,180),
        expand = c(0,0),
        breaks = c(-180,-90,0,90,180),
        labels = c('-180\u00B0','-90\u00B0','0\u00B0','90\u00B0','180\u00B0')
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
       
        panel.grid.minor = element_blank(),
        panel.grid.major = element_line(size = rel(0.2), color = 'grey50'),

        axis.text = element_text(size = 7), 
        axis.title = element_text(size = 7)
    )

ggsave('ramachandran.eps', width = 3, height = 3)


