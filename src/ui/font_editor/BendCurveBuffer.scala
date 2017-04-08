package ui.font_editor

import utilities.MyMath.{MinimizationReport, MinimizationConfig}
import utilities.{MyMath, CubicCurve, Vec2}

/**
  * Created by weijiayi on 5/28/16.
  */
class BendCurveBuffer(
                      start: Vec2, penOffset: Vec2, initCurve: CubicCurve,
                      dotsDistance: Double, dataPointNum: Int, config: MinimizationConfig = BendCurveBuffer.defaultConfig) {
  var dots = IndexedSeq[Vec2](start)

  var curve = initCurve
  var minimizationReport: Option[MinimizationReport] = None

  private def updateCurve() = {
    if(dots.length>=2){
      val sampleDots = (0 to dataPointNum).map{i =>
        val t = i.toDouble/dataPointNum
        MyMath.linearInterpolatePoints(dots)(t)
      }

      val sampleLength = MyMath.totalLength(sampleDots)
      val configToUse = config.copy(errorForStop = config.errorForStop * sampleLength * sampleLength)
      val (r, c) = CubicCurve.dotsToCurve(curveSampleNum = 2*dataPointNum, configToUse)(sampleDots)
      curve = c
      minimizationReport = Some(r)
    }
  }

  def lastPoint = dots.last

  def penMoveTo(pos: Vec2) = {
    val p = pos - penOffset
    if((p-lastPoint).lengthSquared >= dotsDistance*dotsDistance){
      dots = dots :+ p
      updateCurve()
    }
  }

  def report(): Unit = {
    println(
      s""""dots number: ${dots.length}
         | minimization report: $minimizationReport
       """.stripMargin)
  }

}

object BendCurveBuffer{
  def defaultConfig = MinimizationConfig(
    errorForStop = 0.0001,
    maxIterations = 50,
    learningRate = 0.1,
    gradientDelta = 1e-4
  )
}
