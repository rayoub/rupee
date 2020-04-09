
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())
    
get_precision_plot <- function(p_title, p_file, p_levels, p_limits, p_breaks, p_y_axis) {

    # read in data file
    df <- read.csv(p_file)

    # reorder factor levels
    df$app <- factor(df$app, levels = p_levels) 

    # *** plot
    
    plot <- 
        
        ggplot(df, aes(n, level_precision, group = app, color = app, linetype = app)) +

        # geoms
        geom_line(
            size = rel(0.5)
        ) + 

        # scales
        scale_color_manual(
            values = c("#e41a1c", "#377eb8", "#4daf4a", "#984ea3"),
            labels = c("RUPEE TM-Score", "RUPEE RMSD", "RUPEE Fast","Compared To")
        ) + 
        scale_linetype_manual(
            values = c("solid","dashed","dotted", "dotdash"),
            labels = c("RUPEE TM-Score", "RUPEE RMSD", "RUPEE Fast","Compared To")
        ) + 
        scale_x_continuous(
            limits = p_limits,
            breaks = p_breaks
        ) + 
        
        # guides
        guides(linetype = guide_legend(override.aes = list(size = rel(0.5)))) + 

        # axis labels
        labs(
             x = 'rank', 
             y = p_y_axis
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
    
    plot + theme
}


