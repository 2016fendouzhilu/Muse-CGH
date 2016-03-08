package utilities

import math._

/**
 * Functions with a period of 2π
 */
object PeriodicAngularFunctions {
  def sineWave(order: Int, coefficients: IndexedSeq[Double], scale: Double) = (x: Double) => {
    var sum = 0.0
    (0 until order).foreach { i=>
      sum += coefficients(i)*cos(i*x) + coefficients(i+order)*sin((i+1)*x)
    }
    exp(scale * sum)
  }
}
