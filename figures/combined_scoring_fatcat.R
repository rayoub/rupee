
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
        'vs. mTM (scop_d360)',
        'scoring_mtm_fatcat.txt',
        c('RUPEE TM-Score', 'RUPEE RMSD', 'RUPEE Fast', 'mTM'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        TRUE
)
ssm_plot <- get_scoring_plot(
        'vs. SSM (scop_d62)',
        'scoring_ssm_fatcat.txt',
        c('RUPEE TM-Score', 'RUPEE RMSD', 'RUPEE Fast', 'SSM'), 
        c(1, 50),
        c(1, seq(5, 50, by = 5)),
        FALSE
)
cathedral_plot <- get_scoring_plot(
        'vs. CATHEDRAL (cath_d99)',
        'scoring_cathedral_fatcat.txt',
        c('RUPEE TM-Score', 'RUPEE RMSD', 'RUPEE Fast', 'CATHEDRAL'), 
        c(1, 100),
        c(1, seq(10, 100, by = 10)),
        FALSE
)

combined <- grid_arrange_shared_legend(mtm_plot, ssm_plot, cathedral_plot)

ggsave('combined_scoring_fatcat.eps', plot = combined, width = 7, height = 3.5)

