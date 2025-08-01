package io.github.stivens.example.suites

import org.scalatest.funsuite.AnyFunSuite

class TestSuite3 extends AnyFunSuite {

  test("sleep 1 minute and pass") {
    Thread.sleep(60000)
    assert(true)
  }

}
