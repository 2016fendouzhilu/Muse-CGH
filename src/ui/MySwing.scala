package ui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JComboBox, AbstractButton}

import scala.language.reflectiveCalls

/**
  * Created by weijiayi on 2/29/16.
  */
object MySwing {
  def addAction(jButton: AbstractButton, action: ()=>Unit): Unit ={
    jButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = action()
    })
  }

  type ActionSource = { def addActionListener(l: ActionListener): Unit}

  def reactAction(source: ActionSource)(action: => Unit): Unit ={
    source.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = action
    })
  }

//  trait ActionSource{
//    def howToAdd: ActionListener => Unit
//
//    def reactAction(action: =>Unit): Unit ={
//      howToAdd(new ActionListener {
//        override def actionPerformed(e: ActionEvent): Unit = action
//      })
//    }
//  }
//
//  implicit class ButtonIsActionSource(implicit jButton: AbstractButton) extends ActionSource {
//    override def howToAdd: (ActionListener) => Unit = jButton.addActionListener
//  }
//
//  implicit class JComboboxIsActionSource[T](implicit jCombo: JComboBox[T]) extends ActionSource {
//    override def howToAdd: (ActionListener) => Unit = jCombo.addActionListener
//  }
}
