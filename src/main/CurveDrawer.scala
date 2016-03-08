package main

import java.awt.geom.{Ellipse2D, Line2D, Path2D}
import java.awt.{BasicStroke, Color, Graphics2D, RenderingHints}

import render.RenderingSeg
import utilities.{CubicCurve, MyMath, Vec2}

/**
  * Created by weijiayi on 2/29/16.
  */

class CurveDrawer(val g2d: Graphics2D, pointTransform: Vec2 => Vec2, scaleFactor: Double,
                  dotsPerUnit: Double = 20.0, thicknessScale: Double = 1.0) {
  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

  def setColor(color: Color) = g2d.setColor(color)

  def drawCurve(inkCurve: LetterSeg): Unit = inkCurve match{
    case LetterSeg(curve, start, end, _, _) =>
      drawCurveWithWF(curve, MyMath.linearInterpolate(start, end))
  }

  def drawCurveWithWF(curve: CubicCurve, wF: Double => Double, onLinePaint: Vec2 => Unit = (_) => Unit): Unit = {
    val points = curve.samples(dotsPerUnit)
    val tangents = curve.sampleTangents(dotsPerUnit)
    val dots = points.length
    val dt = 1.0/dots

    for(i <- 0 until dots-1){
      val r0 = wF(i*dt)
      val r1 = wF((i+1)*dt)
      val p1 = points(i + 1)
      onLinePaint(p1)
      drawThicknessLine(points(i), p1, tangents(i), tangents(i+1), r0*thicknessScale, r1*thicknessScale)
    }
  }

  def drawRSeg(rSeg: RenderingSeg, onLinePaint: Vec2 => Unit): Unit = rSeg match {
    case RenderingSeg(curve, wF) =>
      drawCurveWithWF(curve, wF, onLinePaint)
      
  }

  def drawCurveControlPoints(inkCurve: LetterSeg, endpointColor: Color, controlColor: Color, lineWidth: Double): Unit = inkCurve match{
    case LetterSeg(curve, start, end, _, _) =>
      setColor(endpointColor)
      drawDot(curve.p0, start)
      drawDot(curve.p3, end)

      val controlR = (start+end)/2
      setColor(controlColor)
      drawDot(curve.p1, controlR)
      drawDot(curve.p2, controlR)
      drawLine(curve.p1,curve.p0,lineWidth, noWidthScale = true)
      drawLine(curve.p2,curve.p3,lineWidth, noWidthScale = true)
  }

  def drawLine(p0: Vec2, p1: Vec2, width: Double, noWidthScale: Boolean = false, dashed: Option[(Float,Float)] = None): Unit ={
    val w = width * (if(noWidthScale) 1.0 else scaleFactor)
    val stroke = dashed match{
      case Some((a,b)) =>
        new BasicStroke(w.toFloat, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, Array(a,b), 0)
      case None => new BasicStroke(w.toFloat)
    }
    g2d.setStroke(stroke)
    val line = new Line2D.Double(pointTransform(p0), pointTransform(p1))
    g2d.draw(line)
  }

  def drawThicknessLine(p0: Vec2, p1: Vec2, tangent0: Vec2, tangent1: Vec2, startWidth: Double, endWidth: Double): Unit = {
    val n0 = Vec2(tangent0.y, -tangent0.x)
    val n1 = Vec2(tangent1.y, -tangent1.x)
    val v0 = pointTransform(p0 + n0 * startWidth/2)
    val v1 = pointTransform(p0 - n0 * startWidth/2)
    val v2 = pointTransform(p1 - n1 * endWidth/2)
    val v3 = pointTransform(p1 + n1 * endWidth/2)
    val path = new Path2D.Double()
    path.moveTo(v0.x, v0.y)
    path.lineTo(v1.x, v1.y)
    path.lineTo(v2.x, v2.y)
    path.lineTo(v3.x, v3.y)
    path.closePath()

    g2d.fill(path)
  }

  def drawDot(center: Vec2, radius: Double): Unit = {
    val c = pointTransform(center)
    val r = radius * scaleFactor
    val dot = new Ellipse2D.Double(c.x-r, c.y-r, 2*r, 2*r)

    g2d.fill(dot)
  }

  def drawLetter(letter: Letter, mainStrokeColor: Color, highlightColor: Color, highlights: Seq[Int]) = {
    letter.segs.zipWithIndex.foreach{case (s, i) =>
      if(!(highlights contains i)){
        setColor(mainStrokeColor)
        drawCurve(s)
      }
    }
    letter.segs.zipWithIndex.foreach{case (s, i) =>
      if(highlights contains i){
        setColor(highlightColor)
        drawCurve(s)
      }
    }
  }
}

object CurveDrawer{
  def colorWithAlpha(c: Color, double: Double): Color = {
    val a = (double * 255).toInt
    new Color(c.getRed, c.getGreen, c.getBlue, a)
  }
}