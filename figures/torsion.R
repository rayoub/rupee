
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

# read in data files
df <- read.csv('torsion.txt')

# wrap and translate torsion angles
df$phi <- ifelse(df$phi < 0, df$phi + 360, df$phi)
df$phi <- df$phi + 200

# reorder factor levels
df$sse <- factor(df$sse, levels = c('Helix', 'Turn', 'Strand', 'Bridge', 'Bend', 'Coil'))

ggplot(df, aes(phi, psi)) +
    
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

    # coordinates 
    coord_polar(
       theta = 'y',
       direction = -1
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
        
        axis.text = element_text(size = 6), 
        axis.title = element_text(size = 7), 
        axis.line = element_blank()
    )

ggsave('torsion.eps', width = 3, height = 3)

