package io.github.stivens.example.suites

import io.github.stivens.example.Common
import org.scalatest.funsuite.AnyFunSuite

class TestSuite16 extends AnyFunSuite {

  test("sleep some time and pass") {
    Common.sleep()
    assert(true)
  }

}
