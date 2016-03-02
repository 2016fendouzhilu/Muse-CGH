package utilities

/**
  * Cubic Bezier Curve
  */
case class CubicCurve(p0: Vec2, p1: Vec2, p2: Vec2, p3: Vec2) {
  def eval(t: Double): Vec2 = {
    val delta = 1 - t
    val dSquare = delta * delta
    val dCubic = dSquare * delta
    p0 * dCubic + p1 * (3*dSquare*t) + p2 * (3*delta*t*t) + p3 * (t*t*t)
  }

  def getPoint(id: Int) = id match{
    case 0 => p0
    case 1 => p1
    case 2 => p2
    case 3 => p3
  }

  def setPoint(id: Int, p: Vec2): CubicCurve = id match {
    case 0 => copy(p0 = p)
    case 1 => copy(p1 = p)
    case 2 => copy(p2 = p)
    case 3 => copy(p3 = p)
  }
}
