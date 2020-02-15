
library(grid)
library(gridExtra)

source('scoring_rupee.R')

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

plot_1 <- get_scoring_plot(
        'scop_d360 benchmark',
        'scoring_rupee_scop_d360.txt',
        c(0.85, 1.00)
)
plot_2 <- get_scoring_plot(
        'casp_d250 benchmark',
        'scoring_rupee_casp_d250.txt',
        c(0.40, 0.55)
)

combined <- grid_arrange_shared_legend(plot_1, plot_2)

ggsave('combined_scoring_rupee.eps', plot = combined, width = 5.2, height = 2.25, dpi = 300)

