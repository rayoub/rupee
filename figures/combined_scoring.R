
library(grid)
library(gridExtra)

source('scoring.R')

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

mtm_plot_d144 <- get_scoring_plot(
        'vs. mTM (casp_mtm_d144)',
        'scoring_mtm_d144.txt',
        c('RUPEE All-Aligned','mTM'), 
        c(1, 10),
        seq(1,10)
)
ssm_plot <- get_scoring_plot(
        'vs. SSM (casp_ssm_d149)',
        'scoring_ssm_d149.txt',
        c('RUPEE All-Aligned','SSM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10))
)
cathedral_plot <- get_scoring_plot(
        'vs. CATHEDRAL (casp_cath_d149)',
        'scoring_cathedral_d149.txt',
        c('RUPEE All-Aligned','CATHEDRAL'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10))
)

combined <- grid_arrange_shared_legend(mtm_plot_d144, ssm_plot, cathedral_plot)

ggsave('combined_scoring.eps', plot = combined, width = 7.5, height = 2.25, dpi = 300)

