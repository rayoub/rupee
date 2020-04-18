
library(grid)
library(gridExtra)

source('scoring.R')

grid_arrange_shared_legend <-
  function(...,
           ncol = 2, 
           nrow = 1, 
           position = c("bottom", "right")) {
    
    plots <- list(...)
    position <- match.arg(position)
    g <- ggplotGrob(plots[[1]] + theme(legend.position = position))$grobs
    legend <- g[[which(sapply(g, function(x) x$name) == "guide-box")]]
    lheight <- sum(legend$height)
    lwidth <- sum(legend$width)
    gl <- lapply(plots, function(x) x + theme(legend.position = "none"))
    gl <- c(gl, ncol = ncol, nrow = nrow)
    
    combined <- switch(
      position,
      "bottom" = arrangeGrob(
        do.call(arrangeGrob, gl),
        legend,
        ncol = 1,
        heights = unit.c(unit(1, "npc") - lheight, lheight)
      ),
      "right" = arrangeGrob(
        do.call(arrangeGrob, gl),
        legend,
        ncol = 2,
        widths = unit.c(unit(1, "npc") - lwidth, lwidth)
      )
    )
  }

plot1 <- get_scoring_plot(
        'mTM-align',
        'vs. mTM-align',
        'TM-score (q)',
        'scoring_mtm_ci.txt', # _rmsd, _ci
        c('RUPEE All-Aligned','RUPEE Top-Aligned','mTM'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.10, 0.65), 
        TRUE
)
plot2 <- get_scoring_plot(
        'mTM-align',
        'vs. mTM-align',
        'RMSD',
        'scoring_mtm_rmsd.txt', # _rmsd, _ci
        c('RUPEE All-Aligned','RUPEE Top-Aligned','mTM'), 
        c(1, 10),
        c(2,4,6,8,10),
        c(0, 5),
        FALSE
)

combined <- grid_arrange_shared_legend(plot1, plot2)

ggsave('combined_scoring_mtm.eps', plot = combined, width = 7.5, height = 3.25, dpi = 300)

