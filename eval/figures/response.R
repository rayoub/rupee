
library(ggplot2)
library(plyr)

get_timing_plot <- function(p_title, p_file, p_levels) {

    # read in data file
    df <- read.csv(p_file)

    # reorder factor levels
    df$app <- factor(df$app, levels = p_levels)

    # *** plot

    plot <- 

        ggplot(df, aes(residue_count, time, group = app, color = app)) +
            
        # geoms
        geom_point(
            size = rel(0.30)
        ) + 
        geom_smooth(     
            size = rel(0.30),
            show.legend = FALSE
        ) + 

        # scales
        scale_color_manual(
            values = c("#e41a1c","#377eb8","#984ea3","#4daf4a"),
            labels = c("All","Top", "Fast", "mTM (left), SSM (right)")
        ) + 
        scale_y_continuous(
            trans = "log10",
            limits = c(1,10000)
        ) + 
    
        # guides
        guides(color = guide_legend(override.aes = list(size = rel(1)))) + 

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
            plot.title = element_text(size = 7),
            plot.margin = margin(5,15,0,5), 

            panel.grid = element_blank(),
            panel.spacing = unit(4,'mm'),

            axis.text = element_text(size = 7), 
            axis.title = element_text(size = 7, margin = margin(0,10,0,0)), 
            
            legend.text = element_text(size = 7),
            legend.title = element_blank(), 
            legend.position = 'bottom',
            legend.direction = 'horizontal',
            legend.spacing = unit(0,'mm')
        ) 

    plot + theme
}
