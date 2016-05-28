package utilities

/**
  * Extra maths
  */
object MyMath {

  def linearInterpolate(x0: Double, x1: Double)(t: Double): Double = {
    x0 + t * (x1-x0)
  }

  def linearInterpolate(x0: Vec2, x1: Vec2)(t: Double): Vec2 = {
    x0 + (x1-x0) * t
  }

  def ceil(x: Double) = x.ceil.toInt

  def wrap(i: Int, max: Int) = {
    val x = i % max
    if(x<0) x+max else x
  }

  /**
    * calculate the new index in an array
    *
    * @param index current pos
    * @param size array size
    * @param offset index offset
    * @return
    */
  def nearIndexOption(index: Int, size: Int, offset: Int): Option[Int] = {
    val ni = index + offset
    if(ni>=0 && ni<size)
      Some(ni)
    else
      None
  }

  def totalLength(points: IndexedSeq[Vec2]): Double = {
    var len = 0.0
    var i = 0
    while (i < points.length - 1) {
      len += (points(i+1)-points(i)).length
      i += 1
    }
    len
  }



  case class MinimizationConfig(gradientForStop: Double, maxIterations: Int, learningRate: Double = 0.1, gradientDelta: Double = 1e-2)

  case class MinimizationReport(iterations: Int, error: Double, lastDeltaError: Double)

  def minimize(f: IndexedSeq[Double] => Double, config: MinimizationConfig)(initParams: IndexedSeq[Double]): (MinimizationReport,IndexedSeq[Double]) = {
    def partialDerivative(params: IndexedSeq[Double] ,paramIndex: Int): Double = {
      val forward = f(params.updated(paramIndex, params(paramIndex)+ config.gradientDelta))
      val backward = f(params.updated(paramIndex, params(paramIndex)- config.gradientDelta))
      (forward - backward) / (2* config.gradientDelta)
    }

    var params = initParams
    var oldError = f(initParams)
    var deltaError = -1.0
    for(i <- 0 until config.maxIterations){
      var gradientSmall = true
      params = params.indices.map{pIdx =>
        val der = partialDerivative(params, pIdx)
        if(math.abs(der) >= config.gradientForStop)
          gradientSmall = false
        params(pIdx) - der * config.learningRate
      }
      val newError = f(params)
      deltaError = newError - oldError
      if(gradientSmall) {
        val report = MinimizationReport(i, newError, lastDeltaError = deltaError)
        return (report, params)
      }
      oldError = newError
    }
    (MinimizationReport(config.maxIterations, oldError, deltaError), params)
  }
}
