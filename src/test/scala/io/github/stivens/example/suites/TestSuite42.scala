package io.github.stivens.example.suites

import io.github.stivens.example.Common
import org.scalatest.funsuite.AnyFunSuite

class TestSuite42 extends AnyFunSuite {

  test("sleep some time and pass -- 1") {
    Common.sleep()
    assert(true)
  }

  test("sleep some time and pass -- 2") {
    Common.sleep()
    assert(true)
  }

  test("sleep some time and pass -- 3") {
    Common.sleep()
    assert(true)
  }

  test("sleep some time and pass -- 4") {
    Common.sleep()
    assert(true)
  }

}
