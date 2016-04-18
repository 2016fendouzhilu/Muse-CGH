package ui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.AbstractButton

/**
  * Created by weijiayi on 2/29/16.
  */
object MyButton {
  def addAction(jButton: AbstractButton, action: ()=>Unit): Unit ={
    jButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = action()
    })
  }
}
