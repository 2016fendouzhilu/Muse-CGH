package ui.font_editor

import utilities.MyMath.{MinimizationReport, MinimizationConfig}
import utilities.{CubicCurve, Vec2}

/**
  * Created by weijiayi on 5/28/16.
  */
class BendCurveBuffer(start: Vec2, penOffset: Vec2, initCurve: CubicCurve, dotsDistance: Double, config: MinimizationConfig = BendCurveBuffer.defaultConfig) {
  var dots = IndexedSeq[Vec2](start)

  var curve = initCurve
  var minimizationReport: Option[MinimizationReport] = None

  private def updateCurve() = {
    if(dots.length>=2){
      val sampleDots =
        if(dots.length < 20) dots
        else {
          val step = dots.length / 10
          (dots.indices by step).map(dots(_))
        }
      val (r, c) = CubicCurve.dotsToCurve(sampleDots, curveSampleNum = 2 * sampleDots.length, config)
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
    gradientForStop = 0.1,
    maxIterations = 50,
    learningRate = 0.1,
    gradientDelta = 1e-8
  )
}
