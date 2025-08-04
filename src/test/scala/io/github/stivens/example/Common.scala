package io.github.stivens.example

object Common {
  def sleep(millis: Long = /* 30s */ 30000): Unit = {
    Thread.sleep(millis)
  }
}
