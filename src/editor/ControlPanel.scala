package editor

import java.awt.{Dimension, FlowLayout}
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import main.InkCurve
import mymath.{Vec2, CubicCurve}

import scala.collection.mutable

/**
  * Created by weijiayi on 2/29/16.
  */

class ControlPanel(editor: Editor) extends JPanel with EditorListener{
  val segmentsPanel = new SegButtonsPanel(i => editor.selectSegment(Some(i)),i=>(),i=>())

  val modes = IndexedSeq(MoveCamera) ++ (0 to 3).map(EditControlPoint)
  val modeButtons = modes.map{ m =>
    val text = m match {
      case MoveCamera => "MoveCamera"
      case EditControlPoint(i) => s"Edit P$i"
    }
    val b = new JRadioButton(text)
    MyButton.addAction(b, ()=>changeMode(m))
    (m,b)
  }

  val alignTangentsCheckbox = new JCheckBox("Align Tangents"){
    MyButton.addAction(this, () => editor.alignTangents = this.isSelected)
    this.doClick()
  }
  val connectNearbyCheckbox = new JCheckBox("Connect Nearby"){
    MyButton.addAction(this, () => editor.connectNearby = this.isSelected)
    this.doClick()
  }

  val insertSegButton = new JButton("Insert Seg")
  MyButton.addAction(insertSegButton, ()=>insertSegment())

  val deleteButton = new JButton("Delete Seg")
  //  MyButton.addAction(deleteButton, ()=>segmentsPanel.deleteButton())

  val undoButton = new JButton("Undo")
  MyButton.addAction(undoButton, () => editor.undo())

  setupLayout()

  def setupLayout(): Unit ={
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

    def addRow(components: JComponent*): Unit ={
      add(new JPanel(){
        setLayout(new FlowLayout())
        components.foreach(this.add)
      })
    }

    addRow(modeButtons.map(_._2) :_*)

    addRow(alignTangentsCheckbox, connectNearbyCheckbox)

    addRow(insertSegButton, deleteButton, undoButton)

    add(segmentsPanel)
    add(new JPanel())
  }

  override def editingUpdated(): Unit = {
    editor.currentEditing() match{
      case Editing(letter, darkness, selects) =>
        segmentsPanel.setButtonCount(letter.segs.length)
        segmentsPanel.setSelected(selects)
    }
    modeButtons.foreach{
      case (m, b) => b.setSelected(m==editor.mode)
    }

  }

  def insertSegment(): Unit ={
    val nextIndex = segmentsPanel.currentSelected.getOrElse(-1) + 1
    editor.insertSegment(nextIndex)
  }

  def changeMode(mode: EditMode): Unit = {
    editor.changeMode(mode)
  }
}

object ControlPanel{

}



class SegButtonsPanel(selectAction: Int=>Unit, deleteAction: Int=>Unit, insertAction: Option[Int]=>Unit) extends JPanel {
  private var buttonCount = 0
  private val buttons: mutable.ArrayBuffer[JToggleButton] = mutable.ArrayBuffer()
  private var selected: Option[Int] = None

  setLayout(new FlowLayout())
  setPreferredSize(new Dimension(400,120))

  def currentSelected = selected

  def makeButton(index: Int): JToggleButton ={
    val b = new JToggleButton(s"$index")
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
    selected.foreach{i =>
      val b = buttons(i)
      buttons.foreach(x => x.setSelected(x == b))
    }
  }

}

class SegButtonsPanel2(selectAction: Int=>Unit, deleteAction: Int=>Unit, insertAction: Option[Int]=>Unit) extends JPanel{
  private val buttons: mutable.ArrayBuffer[JRadioButton] = mutable.ArrayBuffer()
  var selected: Option[Int] = None

  setLayout(new FlowLayout())
  setPreferredSize(new Dimension(400,120))
  val group = new ButtonGroup()

  insertButton()

  def makeButton(index: Int): JRadioButton ={
    val b = new JRadioButton(s"$index")
    group.add(b)
    MyButton.addAction(b, ()=>onSegSelected(index))
    buttons += b
    b
  }

  def insertButton(): Unit ={
    val b = makeButton(buttons.length)

    insertAction(selected)

    this.add(b)
    this.revalidate()

    val nextButtonIndex = selected.getOrElse(-1)+1
    buttons(nextButtonIndex).doClick()
  }

  def onSegSelected(index: Int): Unit ={
    selected = Some(index)

    selectAction(index)
  }

  def deleteButton(): Unit = {
    selected.foreach(s =>{
      val index = buttons.length-1
      val b = buttons.remove(index)
      group.remove(b)
      deleteAction(s)

      this.remove(b)
      this.revalidate()
      this.repaint()

      buttons.headOption match{
        case Some(h) => h.doClick()
        case None => selected = None
      }
    })
  }


}

object SegButtonsPanel{
  def initSeg() =
    InkCurve(
      CubicCurve(Vec2.zero, Vec2.right/2, Vec2.right/2, Vec2.right),
      50, 0.1, 0.1
    )
}
