package render

import main.LetterSeg
import utilities.{CubicCurve, MyMath}


case class RenderingSeg(curve: CubicCurve, widthFunc: Double => Double)

object RenderingSeg {
  def linearThickness(s: LetterSeg) = RenderingSeg(s.curve, MyMath.linearInterpolate(s.startWidth, s.endWidth))
}
