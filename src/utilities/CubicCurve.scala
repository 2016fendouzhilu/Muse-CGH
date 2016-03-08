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

  def evalTangent(t: Double): Vec2 = {
    evalFirstDerivative(t).normalized
  }

  def evalCurvature(t: Double): Double = {
    evalSecondDerivative(t).length / evalFirstDerivative(t).length
  }

  def evalFirstDerivative(t: Double) = {
    val delta = 1 - t
    (p1-p0)*(3*delta*delta) + (p2-p1)*(6*delta*t) + (p3-p2)*(3*t*t)
  }

  def evalSecondDerivative(t: Double) = {
    (p2-p1*2+p0) * (6 * (1-t)) + (p3-p2*2+p1) * (6*t)
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

  def pointsMap(f: Vec2 => Vec2) = CubicCurve(f(p0), f(p1), f(p2), f(p3))

  def translate(offset: Vec2) = this.pointsMap(_ + offset)

  def controlLineLength = {
    (p1-p0).length + (p2-p1).length + (p3-p2).length
  }

  def startPoint = p0

  def endPoint = p3

  def straightLineLength = {
    (startPoint - endPoint).length
  }

  def samples(dotsPerUnit: Double): IndexedSeq[Vec2] = {
    val samples = (controlLineLength * dotsPerUnit).toInt + 1
    val dt = 1.0/samples
    (0 to samples).map { i =>
      val t = dt * i
      eval(t)
    }
  }

  def sampleTangents(dotsPerUnit: Double): IndexedSeq[Vec2] = {
    val samples = (controlLineLength * dotsPerUnit).toInt + 1
    val dt = 1.0/samples
    (0 to samples).map { i =>
      val t = dt * i
      evalTangent(t)
    }
  }
}
