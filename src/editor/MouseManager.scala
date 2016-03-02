package editor

import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.JComponent

import editor.MouseManager._
import mymath.Vec2

/**
  * Created by weijiayi on 2/29/16.
  */
class MouseManager(component: JComponent, onMouseDragged: (Offset,InitPos,CurrentPos)=>Unit, onMouseReleased: ()=>Unit) {
  private var lastPos: Option[(InitPos,LastPos)] = None
  def isMousePressed = lastPos.nonEmpty

  component.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      val p = Vec2(e.getX,e.getY)
      lastPos = Some(p,p)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      lastPos.foreach(_ => onMouseReleased())
      lastPos = None
    }
  })

  component.addMouseMotionListener(new MouseAdapter {
    override def mouseDragged(e: MouseEvent): Unit = {
      lastPos match{
        case Some((initP,lastP)) =>
          val p = Vec2(e.getX,e.getY)
          onMouseDragged(p - lastP, initP, p)
          lastPos = Some(initP, p)
        case _ => ()
      }
    }
  })
}

object MouseManager{
  type InitPos = Vec2
  type CurrentPos = Vec2
  type LastPos = Vec2
  type Offset = Vec2
}