
library(grid)
library(gridExtra)

rm(list = ls())

source('response.R')

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

mtm_plot <- get_timing_plot(
        'RUPEE vs: mTM-align\nbenchmark: casp_d250\nstructure database: PDB chains\nRUPEE search type: Full-Length',
        'response_mtm.txt',
        c('All','Top','Fast', 'mTM')
)
ssm_plot <- get_timing_plot(
        'RUPEE vs: SSM\nbenchmark: casp_ssm_d248\nstructure database: SCOP v1.73\nRUPEE search type: Full-Length',
        'response_ssm.txt',
        c('All','Top','Fast', 'SSM')
)

combined <- grid_arrange_shared_legend(mtm_plot, ssm_plot)

ggsave('combined_response.eps', plot = combined, width = 7, height = 2.5)

