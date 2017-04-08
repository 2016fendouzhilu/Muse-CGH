package ui.font_editor

import java.awt.event.KeyEvent
import javax.swing.JTextField

import rx.Var
import utilities.ValueTextComponent


object RxUi {
  def rxTextField(sVar: Var[String]) = {
    val comp = new JTextField()
    new ValueTextComponent[String, JTextField](
      s => Some(s),
      s => s,
      () => sVar.now,
      s => sVar() = s,
      comp,
      Set(KeyEvent.VK_ENTER),
      Set(KeyEvent.VK_CANCEL)
    )
    comp
  }
}
