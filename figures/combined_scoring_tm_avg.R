
library(grid)
library(gridExtra)

source('scoring.R')

grid_arrange_shared_legend <-
  function(...,
           ncol = length(list(...)),
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

mtm_plot <- get_scoring_plot(
        'vs. mTM (casp_d34)',
        'scoring_mtm_tm_avg.txt',
        c('RUPEE All-Aligned','mTM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        TRUE
)
ssm_plot <- get_scoring_plot(
        'vs. mTM (casp_d34)',
        'scoring_mtm_tm_avg.txt',
        c('RUPEE All-Aligned','mTM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        TRUE
)
cathedral_plot <- get_scoring_plot(
        'vs. mTM (casp_d34)',
        'scoring_mtm_tm_avg.txt',
        c('RUPEE All-Aligned','mTM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        TRUE
)

combined <- grid_arrange_shared_legend(mtm_plot, ssm_plot, cathedral_plot)

ggsave('combined_scoring_tm_avg.eps', plot = combined, width = 4, height = 3.5)

