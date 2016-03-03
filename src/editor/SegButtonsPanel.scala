package editor

import java.awt.{Dimension, FlowLayout}
import javax.swing.{JPanel, JToggleButton}

import main.LetterSeg
import utilities.{CubicCurve, MyMath, Vec2}

import scala.collection.mutable

class SegButtonsPanel(selectAction: Int=>Unit) extends JPanel {
  private val buttons: mutable.ArrayBuffer[JToggleButton] = mutable.ArrayBuffer()
  private var selected: Option[Int] = None
  def buttonCount = buttons.length

  setLayout(new FlowLayout())
  setPreferredSize(new Dimension(400,200))

  def currentSelected = selected

  def makeButton(index: Int): JToggleButton ={
    val b = new JToggleButton(s"$index"){setFocusable(false)}
    MyButton.addAction(b, ()=>selectAction(index))
    buttons += b
    b
  }


  def deleteButton(i: Int): Unit ={
    val b = buttons(i)
    this.remove(b)
    buttons.remove(i)
  }

  def setButtonCount(n: Int): Unit ={
    if(n>buttonCount){
      (buttonCount until n).foreach{i =>
        val b = makeButton(i)
        this.add(b)
      }
    }else if (n<buttonCount){
      (n until buttonCount).reverse.foreach{i => deleteButton(i)}
    }

    this.revalidate()
    this.repaint()
  }

  def setSelected(ss: Seq[Int]): Unit ={
    selected = ss.headOption
    buttons.indices.foreach(i => buttons(i).setSelected(selected.contains(i)))
  }

  def moveSelection(delta: Int): Unit ={
    currentSelected match {
      case None => buttons.headOption.foreach{_.doClick()}
      case Some(s) =>
        val index = MyMath.wrap(s + delta, buttonCount)
        buttons(index).doClick()
    }
  }

}


object SegButtonsPanel{
  def initSeg() =
    LetterSeg(CubicCurve(Vec2.zero, Vec2.right/2, Vec2.right/2, Vec2.right), 50, 0.1, 0.1)
}
