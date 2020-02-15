
library(grid)
library(gridExtra)

source('scoring_at.R')

grid_arrange_shared_legend <-
  function(...,
           ncol = 3,
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

mtm_plot_d246 <- get_scoring_plot(
        'vs. mTM (casp_mtm_d246)',
        'scoring_at_mtm_d246.txt',
        c('RUPEE All-Aligned','RUPEE Top-Aligned','mTM'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100)
)
ssm_plot_d250 <- get_scoring_plot(
        'vs. SSM (casp_d250)',
        'scoring_at_ssm_d250.txt',
        c('RUPEE All-Aligned','RUPEE Top-Aligned','SSM'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100)
)
cathedral_plot_d247 <- get_scoring_plot(
        'vs. CATHEDRAL (casp_cathedral_d247)',
        'scoring_at_cathedral_d247.txt',
        c('RUPEE All-Aligned','RUPEE Top-Aligned','CATHEDRAL'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100)
)

combined <- grid_arrange_shared_legend(mtm_plot_d246, ssm_plot_d250, cathedral_plot_d247)

ggsave('combined_scoring_at.eps', plot = combined, width = 7.5, height = 2.25, dpi = 300)

