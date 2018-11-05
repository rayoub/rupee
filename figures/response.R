
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

get_timing_plot <- function(p_title, p_file, p_levels, p_colors, p_y_axis) {

    # read in data file
    df <- read.csv(p_file)

    # reorder factor levels
    df$app <- factor(df$app, levels = p_levels)

    # *** plot

    plot <- 

        ggplot(df, aes(gram_count, timing, group = app, color = app)) +
            
        # geoms
        geom_point(
            size = rel(0.75)
        ) + 

        # scales
        scale_color_manual(
            values = p_colors,
            labels = p_levels
        ) + 

        # guides
        guides(color = guide_legend(override.aes = list(size = rel(0.5)))) + 

        # axis labels
        labs(
             x = 'residue count', 
             y = 'response time (seconds)'
        ) + 
        
        # title
        ggtitle(p_title)

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

    if (p_y_axis) {
        theme <- theme + theme(axis.title.y = element_text(size = 8))
    }
    else {
        theme <- theme + theme(axis.title.y = element_blank())
    }

    plot + theme
}
