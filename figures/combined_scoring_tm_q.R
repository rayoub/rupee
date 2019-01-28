
library(ggplot2)
library(plyr)
library(grid)
library(gridExtra)

# clear environment
rm(list = ls())

get_special_scoring_plot <- function(p_title, p_file, p_levels, p_limits, p_breaks, p_y_axis) {

    # read in data files
    df <- read.csv(p_file)
    refs <- read.csv('scoring_refs.txt')

    # reorder factor levels 
    df$app <- factor(df$app, levels = p_levels)

    # *** plot

    plot <- 
        
        ggplot(df, aes(n, avg_cume_score, group = interaction(app, score_type), color = app, linetype = app)) +
        
        # geoms
        geom_line(
            size = rel(0.5)
        ) + 

        # scales        
        scale_color_manual(
            values = c("#e41a1c", "#377eb8", "#4daf4a", "#984ea3"),
            labels = c("RUPEE TM-Score","RUPEE RMSD","RUPEE Fast","Compared To")
        ) + 
        scale_linetype_manual(
            values = c("solid", "dashed", "dotted", "dotdash"),
            labels = c("RUPEE TM-Score","RUPEE RMSD","RUPEE Fast","Compared To")
        ) + 
        scale_x_continuous(
            limits = p_limits,
            breaks = p_breaks
        ) + 

        # faceting
        facet_wrap(~score_type, ncol = 1, scales = 'free_y', strip.position = 'left') + 
        
        # guides
        guides(linetype = guide_legend(override.aes = list(ncol = 2, size = rel(0.5)))) + 
        guides(color = guide_legend(nrow = 2, byrow = TRUE)) + 

        # axis labels
        labs(
             x = 'rank', 
             y = 'value'
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
            legend.margin = margin(0,0,0,0),

            strip.background = element_blank(),
            strip.placement = 'outside'
        ) 

    if (p_y_axis) {
        theme <- theme + theme(strip.text = element_text(size = 8))
    }
    else {
        theme <- theme + theme(strip.text = element_blank())
    }

    plot + theme
}

mtm_plot <- get_special_scoring_plot(
        'vs. mTM (scop_d360)',
        'scoring_mtm_tm_q.txt',
        c('RUPEE TM-Score', 'RUPEE RMSD', 'RUPEE Fast', 'mTM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        TRUE
)

ggsave('combined_scoring_tm_q.eps', plot = mtm_plot, width = 2.5, height = 3.5)

