package render

import java.awt.{Color, Graphics2D, Graphics}
import javax.swing.JPanel

import editor.{EditThickness, EditControlPoint, Editing}
import main.{LetterSeg, CurveDrawer}
import utilities.Vec2

/**
  * Created by weijiayi on 3/4/16.
  */
class LetterPainter(g2d: Graphics2D, pixelPerUnit: Double, displayPixelScale: Double, imageOffset: Vec2,
                    dotsPerUnit:Double, thicknessScale: Double) {

  def pointTrans(p: Vec2): Vec2 = {
    val s = pixelPerUnit*displayPixelScale
    Vec2(p.x*s, p.y*s) + imageOffset
  }

  val drawer = new CurveDrawer(g2d, pointTrans, pixelPerUnit*displayPixelScale, dotsPerUnit, thicknessScale)

  def draw(segs: IndexedSeq[LetterSeg], color: Color): Unit = {
    drawer.setColor(color)
    segs.foreach(drawer.drawCurve)
  }
}
