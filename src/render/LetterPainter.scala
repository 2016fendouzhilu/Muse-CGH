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

  def draw(segs: IndexedSeq[RenderingSeg], offset: Vec2, color: Color, onLinePaint: Vec2 => Unit = (_) => Unit): Unit = {
    def pointTrans(p: Vec2): Vec2 = {
      val s = pixelPerUnit*displayPixelScale
      (p+offset)*s + imageOffset
    }

    val drawer = new CurveDrawer(g2d, pointTrans, pixelPerUnit*displayPixelScale, dotsPerUnit, thicknessScale)

    drawer.setColor(color)
    segs.foreach{s =>
      drawer.drawRSeg(s, onLinePaint)
    }
  }
}
