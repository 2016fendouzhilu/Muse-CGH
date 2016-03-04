package render

import main.LetterSeg
import utilities.{CubicCurve, Vec2}

/**
  * Created by weijiayi on 3/4/16.
  */
class AffineTransform(ratio: Double) {
  def transformPoint(p: Vec2) = {
    Vec2(p.x + p.y*ratio, p.y)
  }

  def transformCurve(c: CubicCurve) = c.map(transformPoint)

  def transformSeg(s: LetterSeg) = s.copy(curve = transformCurve(s.curve))

}
