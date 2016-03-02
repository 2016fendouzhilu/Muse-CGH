package main

import mymath.{Vec2, CubicCurve}

case class InkCurve(curve: CubicCurve, dots: Int, startWidth: Double, endWidth: Double) {
  def setPoint(id: Int, p: Vec2) = {
    val nc = curve.setPoint(id, p)
    this.copy(curve = nc)
  }
}
