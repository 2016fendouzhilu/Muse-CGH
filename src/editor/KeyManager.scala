package editor

import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.JPanel

import utilities.Vec2

/**
  * Created by weijiayi on 2/29/16.
  */
class KeyManager(component: JPanel, moveAction: Vec2=>Unit) {

  var shiftPressed = false

  component.setFocusable(true)
  component.addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = e.getKeyCode match {
      case KeyEvent.VK_SHIFT => shiftPressed = true
      case _ => ()
    }

    override def keyReleased(e: KeyEvent): Unit = {
      println("released")
      e.getKeyCode match {
        case KeyEvent.VK_SHIFT => shiftPressed = false
        case _ => ()
      }
      var move = Vec2.zero
      if(e.getKeyCode == KeyEvent.VK_RIGHT)
        move += Vec2.right
      else if(e.getKeyCode == KeyEvent.VK_LEFT)
        move += Vec2.left
      else if(e.getKeyCode == KeyEvent.VK_UP)
        move += Vec2.up
      else if(e.getKeyCode == KeyEvent.VK_DOWN)
        move += Vec2.down
      if(move != Vec2.zero)
        moveAction(move)
    }
  })
}
