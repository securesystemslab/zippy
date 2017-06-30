binadd <- function(a, b) {
  res <- a + b
}

binadd(2,3)

.fastr.interop.export('binadd', binadd)
