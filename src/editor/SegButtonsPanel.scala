package editor

import java.awt.{Dimension, FlowLayout}
import javax.swing.{JToggleButton, JPanel}

import main.InkCurve
import utilities.{Vec2, CubicCurve, MyMath}

import scala.collection.mutable

class SegButtonsPanel(selectAction: Int=>Unit, deleteAction: Int=>Unit, insertAction: Option[Int]=>Unit) extends JPanel {
  private var buttonCount = 0
  private val buttons: mutable.ArrayBuffer[JToggleButton] = mutable.ArrayBuffer()
  private var selected: Option[Int] = None

  setLayout(new FlowLayout())
  setPreferredSize(new Dimension(400,120))

  def currentSelected = selected

  def makeButton(index: Int): JToggleButton ={
    val b = new JToggleButton(s"$index"){setFocusable(false)}
    MyButton.addAction(b, ()=>onSegSelected(index))
    buttons += b
    b
  }


  def onSegSelected(index: Int): Unit ={
    selected.foreach(i=>buttons(i).setSelected(false))
    selected = Some(index)

    selectAction(index)
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
      (n until buttonCount).foreach{i => deleteButton(i)}
    }

    buttonCount = n
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
    InkCurve(
      CubicCurve(Vec2.zero, Vec2.right/2, Vec2.right/2, Vec2.right),
      50, 0.1, 0.1
    )
}
