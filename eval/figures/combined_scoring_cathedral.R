
library(grid)
library(gridExtra)

rm(list = ls())

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
        'CATHEDRAL',
        'vs: CATHEDRAL\nbenchmark: casp_cathedral_d247\nstructure database: CATH v4.2\nRUPEE search type: SSAP-score',
        'SSAP-score',
        'scoring_cathedral_ssap.txt',
        c('All','Top', 'Fast','CATHEDRAL'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(60, 75) 
)
plot2 <- get_scoring_plot(
        'CATHEDRAL',
        'vs: CATHEDRAL\nbenchmark: casp_cathedral_d247\nstructure database: CATH v4.2\nRUPEE search type: Full-Length',
        'TM-score (avg)',
        'scoring_cathedral_fl.txt', 
        c('All','Top', 'Fast','CATHEDRAL'), 
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.30, 0.55) 
)

combined <- grid_arrange_shared_legend(plot1, plot2)

ggsave('combined_scoring_cathedral.eps', plot = combined, width = 7.5, height = 2.75, dpi = 300)

