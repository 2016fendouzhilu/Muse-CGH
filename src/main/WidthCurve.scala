package main

import utilities.{CubicCurve, MyMath}


case class WidthCurve(curve: CubicCurve, widthFunc: Double => Double)

object WidthCurve {
  def linearThickness(s: LetterSeg) = WidthCurve(s.curve, MyMath.linearInterpolate(s.startWidth, s.endWidth))
}
