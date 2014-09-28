panel.displayLayoutInfo <-
function(...) {
  cat("row:", current.row(...), "col:", current.column(...), "panel:", panel.number(...), "packet:", packet.number(...), "which.packet:", which.packet(...), "\n")
}
