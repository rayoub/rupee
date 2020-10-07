
library(grid)
library(gridExtra)

rm(list = ls())

source('response_rupee.R')

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

plot1 <- get_timing_plot(
        'RUPEE vs: Exhaustive\nbenchmark: scop_d360\nstructure database: SCOP v2.07\nRUPEE search type: Full-Length',
        'response_rupee_scop.txt',
        c('All','Top','Fast','Exhaustive')
)
plot2 <- get_timing_plot(
        'RUPEE vs: Exhaustive\nbenchmark: casp_d250\nstructure database: SCOP v2.07\nRUPEE search type: Full-Length',
        'response_rupee_casp.txt',
        c('All','Top','Fast','Exhaustive')
)

combined <- grid_arrange_shared_legend(plot1, plot2)

ggsave('combined_response_rupee.eps', plot = combined, width = 7, height = 2.5)

