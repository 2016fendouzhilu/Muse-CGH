package editor

import java.awt.Point
import java.awt.event.{MouseEvent, MouseAdapter}
import javax.swing.JComponent

import mymath.Vec2

/**
  * Created by weijiayi on 2/29/16.
  */
class MouseManager(component: JComponent, onMouseDragged: Vec2=>Unit, onMouseReleased: ()=>Unit) {
  private var initPos: Option[Vec2] = None
  def isMousePressed = initPos.nonEmpty

  component.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      initPos = Some(Vec2(e.getX,e.getY))
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      initPos.foreach(_ => onMouseReleased())
      initPos = None
    }
  })

  component.addMouseMotionListener(new MouseAdapter {
    override def mouseDragged(e: MouseEvent): Unit = {
      initPos match{
        case Some(oldP) =>
          val p = Vec2(e.getX,e.getY)
          onMouseDragged(p - oldP)
          initPos = Some(p)
        case _ => ()
      }
    }
  })
}
