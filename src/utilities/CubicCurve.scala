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

  def samples(sampleNum: Int): IndexedSeq[Vec2] = {
    val dt = 1.0/sampleNum
    (0 to sampleNum).map { i =>
      val t = dt * i
      eval(t)
    }
  }

  def samples(dotsPerUnit: Double): IndexedSeq[Vec2] = {
    val sampleNum = (controlLineLength * dotsPerUnit).toInt + 1
    samples(sampleNum)
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

object CubicCurve{
  /**
    * O(n*n) running time, where n is the number of dots
    */
  def dotsToCurve(dots: IndexedSeq[Vec2], curveSampleNum: Int, config: MyMath.MinimizationConfig): (MyMath.MinimizationReport ,CubicCurve) = {
    def curveError(curve: CubicCurve): Double = {
      val curveSamples = curve.samples(sampleNum = curveSampleNum)
      val dotsErroSum = dots.map{ dot => curveSamples.map(s => (s-dot).lengthSquared).min }.sum
      math.sqrt(dotsErroSum) / MyMath.totalLength(curveSamples)
    }

    val p0 = dots.head
    val p3 = dots.last

    def parameterError(params: IndexedSeq[Double]) = params match{
      case IndexedSeq(p1x,p1y,p2x,p2y) => curveError(CubicCurve(p0,Vec2(p1x,p1y), Vec2(p2x,p2y), p3))
    }

    val initParams = {
      val p1 = MyMath.linearInterpolate(p0,p3)(0.25)
      val p2 = MyMath.linearInterpolate(p0,p3)(0.75)
      IndexedSeq(p1.x,p1.y,p2.x,p2.y)
    }

    val (report, optParams) = MyMath.minimize(parameterError, config)(initParams)
    (report, CubicCurve(p0,Vec2(optParams(0),optParams(1)), Vec2(optParams(2), optParams(3)), p3))
  }
}





