package editor

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import java.io.{File, FileFilter}
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import utilities.EditingSaver

/**
  * Created by weijiayi on 2/29/16.
  */

class ControlPanel(editor: Editor, zoomAction: Double => Unit) extends JPanel with EditorListener{
  val segmentsPanel = new SegButtonsPanel(i => editor.selectSegment(Some(i)))

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

  def makeButton(name: String) (action: => Unit): JButton = {
    val b = new JButton(name){ setFocusable(false) }
    MyButton.addAction(b, ()=>action)
    b
  }

  val appendButton = makeButton("Append Seg"){ editor.appendSegment() }

  val cutSegmentButton = makeButton("Cut Seg"){ cutSegment() }

  val deleteButton = makeButton("Delete Seg"){ deleteSegment() }

  val undoButton = makeButton("Undo"){ editor.undo() }

  val redoButton = makeButton("Redo"){ editor.redo() }

  val saveButton = makeButton("Save"){ saveEditing() }

  val loadButton = makeButton("Load"){ loadEditing() }

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

    addRow(appendButton, cutSegmentButton, deleteButton, undoButton, redoButton, saveButton, loadButton)

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

  def fontFileFilter = new FileNameExtensionFilter("Muse Editing Files", "muse")

  def saveEditing(): Unit = {
    import java.nio.file.Paths
    val fc = new JFileChooser(Paths.get("").toAbsolutePath.toFile){
      setFileFilter(fontFileFilter)
    }
    fc.showSaveDialog(this) match {
      case JFileChooser.APPROVE_OPTION =>
        val select = fc.getSelectedFile.getAbsolutePath
        val path = if(select.endsWith(".muse")) select else select+".muse"
        EditingSaver.saveToFile(new File(path), editor.currentEditing())
      case _ => ()
    }
  }

  def loadEditing(): Unit = {
    import java.nio.file.Paths
    val fc = new JFileChooser(Paths.get("").toAbsolutePath.toFile){
      setFileFilter(fontFileFilter)
      setMultiSelectionEnabled(false)
    }
    fc.showOpenDialog(this) match {
      case JFileChooser.APPROVE_OPTION =>
        val select = fc.getSelectedFile
        EditingSaver.loadFromFile(select) match {
          case Some(e) => editor.editAndRecord(e)
          case None => println("failed to load file!")
        }
      case _ => ()
    }
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
        case VK_ESCAPE =>
          editor.selectSegment(None)
        case VK_Z =>
          if(e.isControlDown || e.isAltDown)
            undoButton.doClick()
        case VK_R =>
          if(e.isControlDown || e.isAltDown)
            redoButton.doClick()
        case VK_UP =>
          zoomAction(1.25)
        case VK_DOWN =>
          zoomAction(0.8)
        case _ => ()
      }
      val keyId = e.getKeyCode - VK_1 + 1
      if(keyId>=1 && keyId<=6)
        modeButtonPairs(keyId)._2.doClick()
    }
  }
}



