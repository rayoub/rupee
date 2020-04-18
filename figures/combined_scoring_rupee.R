
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

plot1 <- get_scoring_plot(
        'All vs. Top (benchmark scop_d360)',
        'scoring_rupee_scop_d360.txt',
        c('RUPEE All-Aligned','RUPEE Top-Aligned'),
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.85, 1.0)
)
plot2 <- get_scoring_plot(
        'All vs. Top (benchmark casp_d250)',
        'scoring_rupee_casp_d250.txt', 
        c('RUPEE All-Aligned','RUPEE Top-Aligned'),
        c(1, 100),
        c(1,10,20,30,40,50,60,70,80,90,100),
        c(0.4, 0.55)
)

combined <- grid_arrange_shared_legend(plot1, plot2)

ggsave('combined_scoring_rupee.eps', plot = combined, width = 7.5, height = 3.25, dpi = 300)

