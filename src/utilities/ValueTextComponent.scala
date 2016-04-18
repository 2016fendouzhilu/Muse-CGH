package utilities

import java.awt.event.{FocusAdapter, FocusEvent, KeyAdapter, KeyEvent}
import javax.swing.text.JTextComponent

import utilities.ValueTextComponent.KeyCode

/**
 * Created by weijiayi on 3/6/16.
 */
object ValueTextComponent {
  type KeyCode = Int
}

class ValueTextComponent[T,Comp<: JTextComponent](textToValue: String => Option[T], valueToText: T => String,
                           getValue: ()=>T, setValue: T => Unit, val component: Comp,
                           confirmKeys: Set[KeyCode], cancelKeys: Set[KeyCode]) {

  component.addFocusListener(new FocusAdapter {
    override def focusLost(e: FocusEvent): Unit = { tryChangeValue()}
  })

  component.addKeyListener(new KeyAdapter {
    override def keyReleased(e: KeyEvent): Unit = {
      if(confirmKeys contains e.getKeyCode) { tryChangeValue() }
      else if(cancelKeys contains e.getKeyCode) { updateText() }
    }
  })

  def updateText(): Unit = {
    val text = valueToText(getValue())
    if (component.getText != text)
      component.setText(text)
  }

  def tryChangeValue(): Unit = {
    val t = component.getText
    textToValue(t) match {
      case Some(v) =>
        setValue(v)
        updateText()
      case None => updateText()
    }
  }
}
