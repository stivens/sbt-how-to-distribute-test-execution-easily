package io.github.stivens.example

import java.time.LocalDateTime

@SuppressWarnings(
  Array(
    "scalafix:DisableSyntax.var"
  )
)
object Common {
  def sleep(seconds: Int = 15): Unit = {
    val start         = LocalDateTime.now()
    var lastYieldTime = start
    while (LocalDateTime.now().isBefore(start.plusSeconds(seconds))) {
      if (LocalDateTime.now().isAfter(lastYieldTime.plusSeconds(1))) {
        lastYieldTime = LocalDateTime.now()
        println("busy waiting")
      }
    }
  }
}
