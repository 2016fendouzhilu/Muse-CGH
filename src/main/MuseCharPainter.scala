package main

import java.awt.image.BufferedImage
import java.awt.{RenderingHints, Color, Graphics2D}

import utilities.Vec2

/**
  * CurveDrawer wrapper to set up point transformation
  */
class MuseCharPainter(g2d: Graphics2D, pixelPerUnit: Double, displayPixelScale: Double, imageOffset: Vec2,
                    dotsPerUnit:Double, thicknessScale: Double) {

  def bufferOffset = Vec2(2,2)
  def paintWordWithBuffering(segs: IndexedSeq[WidthCurve], offset: Vec2, color: Color, width: Double, height: Double): Unit = {
    val s = pixelPerUnit*displayPixelScale

    def pointTrans(p: Vec2) = p*s

    val bufferWidth = (width+2*bufferOffset.x)*s
    val bufferHeight = (height+2*bufferOffset.y)*s

    val buffer = new BufferedImage(bufferWidth.toInt, bufferHeight.toInt, BufferedImage.TYPE_INT_ARGB)
    val bufferG = buffer.getGraphics.asInstanceOf[Graphics2D]

    val drawer = new CurveDrawer(bufferG, pointTrans, pixelPerUnit*displayPixelScale, dotsPerUnit, thicknessScale)

    drawer.setColor(color)
    segs.foreach{ case WidthCurve(curve, wf) =>
      drawer.drawColorfulCurve(curve, wf, None)
    }


    val dest = (offset-bufferOffset)*s

    g2d.drawImage(buffer, dest.x.toInt, dest.y.toInt, null)
  }

  def draw(segs: IndexedSeq[WidthCurve], offset: Vec2, color: Color): Unit = {
    def pointTrans(p: Vec2): Vec2 = {
      val s = pixelPerUnit*displayPixelScale
      (p+offset)*s + imageOffset*displayPixelScale
    }

    val drawer = new CurveDrawer(g2d, pointTrans, pixelPerUnit*displayPixelScale, dotsPerUnit, thicknessScale)

    drawer.setColor(color)
    segs.foreach{ case WidthCurve(curve, wf) =>
      drawer.drawCurveWithTimeUsed(curve, wf)
    }
  }

  def drawAnimation(bufferScaleFactor: Double, timeUsed: Double => Boolean = (_) => false)(
    segs: IndexedSeq[WidthCurve], offset: Vec2, color: Color): Boolean = {
    val s = pixelPerUnit*displayPixelScale
    def pointTrans(p: Vec2): Vec2 = {
      ((p+offset)*s + imageOffset*displayPixelScale)* bufferScaleFactor
    }

    val drawer = new CurveDrawer(g2d, pointTrans, s, dotsPerUnit, thicknessScale)

    drawer.setColor(color)

    var lastPos = Vec2.zero
    segs.foreach{case WidthCurve(curve, wf) =>
      val dis = (curve.p0 - lastPos).length
      lastPos = curve.p0

      if(timeUsed(dis) ||
        drawer.drawCurveWithTimeUsed(curve, wf, timeUsed)) return true
    }
    false
  }
}
