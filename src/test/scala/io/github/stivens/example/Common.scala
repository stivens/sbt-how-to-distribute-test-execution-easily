package io.github.stivens.example

import java.time.LocalDateTime

object Common {
  def sleep(seconds: Int = 30): Unit = {
    val start = LocalDateTime.now()
    while (LocalDateTime.now().isBefore(start.plusSeconds(seconds))) {
      println("busy waiting")
    }
  }
}
