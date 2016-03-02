package main

import mymath.{CubicCurve, Vec2}

/**
  * An extension to CubicCurve with Drawing information
  * @param curve
  * @param dots
  * @param startWidth
  * @param endWidth
  */
case class InkCurve(curve: CubicCurve, dots: Int, startWidth: Double, endWidth: Double) {
  def setPoint(id: Int, p: Vec2) = {
    val nc = curve.setPoint(id, p)
    this.copy(curve = nc)
  }
}

object InkCurve{
  val minimalWidth = 0.001
}