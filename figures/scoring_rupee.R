
library(ggplot2)
library(plyr)

# clear environment
rm(list = ls())

get_scoring_plot <- function(p_title, p_file, p_levels, p_xlimits, p_xbreaks, p_ylimits) {

    # read in data files
    df <- read.csv(p_file)

    # reorder factor levels 
    df$app <- factor(df$app, levels = p_levels)

    # *** plot

    plot <- 
        
        ggplot(df, aes(n, avg_cume_score, group = app, color = app, linetype = app)) +
        
        # geoms
        geom_line(
            size = rel(0.5)
        ) + 
        geom_hline(
            yintercept = 0.50,
            colour = 'grey50', 
            size = rel(0.15)
        ) +
        geom_hline(
            yintercept = 0.17,
            colour = 'grey50', 
            size = rel(0.15)
        ) + 

        # scales        
        scale_color_manual(
            values = c("#e41a1c","#377eb8"),
            labels = c("RUPEE All-Aligned","RUPEE Top-Aligned")
        ) + 
        scale_linetype_manual(
            values = c("solid","dashed"),
            labels = c("RUPEE All-Aligned","RUPEE Top-Aligned")
        ) + 
        scale_x_continuous(
            limits = p_xlimits,
            breaks = p_xbreaks
        ) + 
        scale_y_continuous(
            limits = p_ylimits
        ) + 

        # guides
        guides(linetype = guide_legend(override.aes = list(size = rel(0.5)))) + 

        # axis label
        ylab('TM-score (avg)') + 

        # title
        ggtitle(p_title)

    # *** theme
    
    theme <-  

        # default theme 
        theme_bw() +

        # default override
        theme(
            plot.title = element_text(size = 7),
            plot.margin = margin(5,15,0,0), 

            panel.grid = element_blank(),
            panel.spacing = unit(4,'mm'),
            
            axis.text = element_text(size = 7), 
            axis.title.x = element_blank(), 
            axis.title.y = element_text(size = 7),
            
            legend.text = element_text(size = 7, margin = margin(0,10,0,0)),
            legend.title = element_blank(), 
            legend.position = 'bottom',
            legend.direction = 'horizontal',
            legend.spacing = unit(0,'mm')
        ) 

    plot + theme
}



