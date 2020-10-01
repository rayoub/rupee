
library(grid)
library(gridExtra)

rm(list = ls())

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

plot1 <- get_scoring_plot(
        'RUPEE vs: RUPEE\nbenchmark: casp_d250\nstructure database: SCOP v2.07\nRUPEE search type: Full-Length',
        'scoring_rupee_casp_d250.txt', 
        c('All','Top','Fast','Optimal'),
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.35, 0.55)
)
plot2 <- get_scoring_plot(
        'RUPEE vs: RUPEE\nbenchmark: scop_d360\nstructure database: SCOP v2.07\nRUPEE search type: Full-Length',
        'scoring_rupee_scop_d360.txt', 
        c('All','Top','Fast','Optimal'),
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.80, 1.0)
)

combined <- grid_arrange_shared_legend(plot1, plot2)

ggsave('combined_scoring_rupee.eps', plot = combined, width = 5.2, height = 2.4, dpi = 300)

