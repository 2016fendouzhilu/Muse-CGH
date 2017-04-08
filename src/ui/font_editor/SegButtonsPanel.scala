package ui.font_editor

import java.awt.{Dimension, FlowLayout}
import javax.swing.{JPanel, JToggleButton}

import ui.MySwing
import utilities.MyMath

import scala.collection.mutable

class SegButtonsPanel(selectAction: Seq[Int]=>Unit) extends JPanel {
  private val buttons: mutable.ArrayBuffer[JToggleButton] = mutable.ArrayBuffer()
  private var selected: Seq[Int] = IndexedSeq()
  def buttonCount = buttons.length

  setLayout(new FlowLayout())
  setPreferredSize(new Dimension(400,200))

  def currentSelected = selected

  def makeButton(index: Int): JToggleButton ={
    val b = new JToggleButton(s"$index"){setFocusable(false)}
    MySwing.addAction(b, ()=> {
      selectAction(IndexedSeq(index))
    })
    buttons += b
    b
  }

  def selectMore(): Unit = {
    val i0 = currentSelected.last
    val i1 = i0 + 1
    if(i1<buttonCount)
      selectAction(currentSelected :+ i1)
  }

  def selectLess(): Unit = {
    selectAction(currentSelected.drop(1))
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
    selected = ss
    buttons.indices.foreach(i => buttons(i).setSelected(selected.contains(i)))
  }

  def moveSelection(delta: Int): Unit = {
    if(currentSelected.isEmpty)
      buttons.headOption.foreach(_.doClick())
    else if(currentSelected.length == 1){
      currentSelected.foreach(i => {
        val i1 = MyMath.wrap(i + delta, buttonCount)
        buttons(i1).doClick()
      })
    } else {
      val selects = currentSelected.filter(i => {
        val i1 = i + delta
        0 <= i1 && i1 < buttonCount
      }).map(_ + delta)
      selectAction(selects)
    }
  }
}



