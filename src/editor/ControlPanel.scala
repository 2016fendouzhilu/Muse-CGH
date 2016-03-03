package editor

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import javax.swing._

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
    MyButton.addAction(this, () => editor.setAlignTangent(this.isSelected))
    setFocusable(false)
  }
  val strokeBreakCheckbox = new JCheckBox("Stroke Break"){
    MyButton.addAction(this, () => editor.setStrokeBreak(this.isSelected))
    setFocusable(false)
  }

  val appendButton = new JButton("Append Seg"){setFocusable(false)}
  MyButton.addAction(appendButton, ()=>editor.appendSegment())

  val cutSegmentButton = new JButton("Cut Seg"){setFocusable(false)}
  MyButton.addAction(cutSegmentButton, ()=>cutSegment())

  val deleteButton = new JButton("Delete Seg"){setFocusable(false)}
    MyButton.addAction(deleteButton, ()=>deleteSegment())

  val undoButton = new JButton("Undo"){setFocusable(false)}
  MyButton.addAction(undoButton, () => editor.undo())



  setupLayout()

  def setupLayout(): Unit ={
    val contentPanel = new JPanel{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    }

    this.add(contentPanel)

    def addRow(components: JComponent*): Unit ={
      contentPanel.add(new JPanel(){
        setLayout(new FlowLayout())
        setPreferredSize(new Dimension(400,80))
        setMinimumSize(new Dimension(0,0))
        components.foreach(this.add)
      })
    }

    addRow(modeButtonPairs.map(_._2) :_*)

    addRow(alignTangentsCheckbox, strokeBreakCheckbox)

    addRow(appendButton, cutSegmentButton, deleteButton, undoButton)

    contentPanel.add(segmentsPanel)
  }

  override def editingUpdated(): Unit = {
    editor.currentEditing() match{
      case editing@Editing(letter, selects) =>
        segmentsPanel.setButtonCount(letter.segs.length)
        segmentsPanel.setSelected(selects)
        val selectBoxes = Seq(alignTangentsCheckbox,strokeBreakCheckbox)
        editing.selectedInkCurves.headOption match {
          case Some(seg) =>
            selectBoxes.foreach(_.setEnabled(true))
            alignTangentsCheckbox.setSelected(seg.alignTangent)
            strokeBreakCheckbox.setSelected(seg.isStrokeBreak)
          case None =>
            selectBoxes.foreach(_.setEnabled(false))
        }
    }
    modeButtonPairs.foreach{
      case (m, b) => b.setSelected(m==editor.mode)
    }
  }

  def cutSegment(): Unit ={
    segmentsPanel.currentSelected.foreach{ i =>
      editor.cutSegment(i)
    }
  }

  def deleteSegment(): Unit ={
    segmentsPanel.currentSelected.foreach( i =>
      editor.deleteSegment(i)
    )
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
      if(keyId>=1 && keyId<=6)
        modeButtonPairs(keyId)._2.doClick()
    }
  }
}



