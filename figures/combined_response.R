
library(grid)
library(gridExtra)

source('response.R')

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
    gl <- lapply(list(plots[[2]], plots[[3]]), function(x) x + theme(legend.position = "none"))
    gl <- c(gl, ncol = ncol - 1, nrow = nrow)
    
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

dummy_plot <- get_timing_plot(
        'dummy timing',
        'response_dummy.txt',
        c('RUPEE', 'RUPEE Fast', 'mTM', 'SSM','CATHEDRAL'), 
        c('#e41a1c', '#377eb8', '#4daf4a', '#984ea3','#ff7f00'),
        TRUE
)
scop_plot <- get_timing_plot(
        'scop_d62 timing',
        'response_scop_d62.txt',
        c('RUPEE', 'RUPEE Fast', 'mTM', 'SSM'), 
        c('#e41a1c', '#377eb8', '#4daf4a', '#984ea3'),
        TRUE
)
cath_plot <- get_timing_plot(
        'cath_d99 timing',
        'response_cath_d99.txt',
        c('RUPEE', 'RUPEE Fast', 'CATHEDRAL'), 
        c('#e41a1c', '#377eb8', '#ff7f00'),
        FALSE
)

combined <- grid_arrange_shared_legend(dummy_plot, scop_plot, cath_plot)

ggsave('combined_response.eps', plot = combined, width = 7, height = 2.5)

