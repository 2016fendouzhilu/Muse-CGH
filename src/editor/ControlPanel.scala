package editor

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import javax.swing._

import main.InkCurve
import mymath.{CubicCurve, MyMath, Vec2}

import scala.collection.mutable

/**
  * Created by weijiayi on 2/29/16.
  */

class ControlPanel(editor: Editor) extends JPanel with EditorListener{
  val segmentsPanel = new SegButtonsPanel(i => editor.selectSegment(Some(i)),i=>(),i=>())

  val modes = IndexedSeq(MoveCamera) ++ (0 to 3).map(EditControlPoint) ++ IndexedSeq(EditThickness(true), EditThickness(false))
  val modeButtonPairs = modes.map{ m =>
    val text = m match {
      case MoveCamera => "MoveCamera"
      case EditControlPoint(i) => s"Edit P$i"
      case EditThickness(isHead) => s"Edit ${if(isHead) "Head" else "Tail"}"
    }
    val b = new JRadioButton(text){
      setFocusable(false)
      MyButton.addAction(this, ()=>changeMode(m))
    }
    (m,b)
  }

  val alignTangentsCheckbox = new JCheckBox("Align Tangents"){
    MyButton.addAction(this, () => editor.alignTangents = this.isSelected)
    setFocusable(false)
    this.doClick()
  }
  val connectNearbyCheckbox = new JCheckBox("Connect Nearby"){
    MyButton.addAction(this, () => editor.connectNearby = this.isSelected)
    setFocusable(false)
    this.doClick()
  }

  val insertSegButton = new JButton("Insert Seg"){setFocusable(false)}
  MyButton.addAction(insertSegButton, ()=>insertSegment())

  val deleteButton = new JButton("Delete Seg"){setFocusable(false)}
  //  MyButton.addAction(deleteButton, ()=>segmentsPanel.deleteButton())

  val undoButton = new JButton("Undo"){setFocusable(false)}
  MyButton.addAction(undoButton, () => editor.undo())



  setupLayout()

  def setupLayout(): Unit ={
    val contentPanel = new JPanel{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    }
//    val scrollPane = new JScrollPane(contentPanel){
//      setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
//    }
    this.add(contentPanel)

    def addRow(components: JComponent*): Unit ={
      contentPanel.add(new JPanel(){
        setLayout(new FlowLayout())
        setPreferredSize(new Dimension(400,50))
        setMinimumSize(new Dimension(0,0))
        components.foreach(this.add)
      })
    }

    addRow(modeButtonPairs.map(_._2) :_*)

    addRow(alignTangentsCheckbox, connectNearbyCheckbox)

    addRow(insertSegButton, deleteButton, undoButton)

    contentPanel.add(segmentsPanel)
  }

  override def editingUpdated(): Unit = {
    editor.currentEditing() match{
      case Editing(letter, selects) =>
        segmentsPanel.setButtonCount(letter.segs.length)
        segmentsPanel.setSelected(selects)
    }
    modeButtonPairs.foreach{
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

  def makeKeyListener() = new KeyAdapter {
    override def keyReleased(e: KeyEvent): Unit = {
      import KeyEvent._
      e.getKeyCode match {
        case VK_BACK_QUOTE =>
          modeButtonPairs.head._2.doClick()
        case VK_RIGHT =>
          segmentsPanel.moveSelection(1)
        case VK_LEFT =>
          segmentsPanel.moveSelection(-1)
        case _ => ()
      }
      val keyId = e.getKeyCode - VK_1 + 1
      if(keyId>=1 && keyId<=4)
        modeButtonPairs(keyId)._2.doClick()
    }
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
    selected.foreach{i =>
      val b = buttons(i)
      buttons.foreach(x => x.setSelected(x == b))
    }
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
