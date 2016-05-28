package ui.font_editor

import java.awt.event.{ItemEvent, ItemListener, KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import ui.MyButton
import main.MuseCharType
import utilities.{ChangeListener, EditingSaver}

/**
  * Control Panel for font editor
  */

class ControlPanel(editor: EditorCore, zoomAction: Double => Unit) extends JPanel with ChangeListener{
  val segmentsPanel = new SegButtonsPanel(i => editor.selectSegment(Some(i)))

  val modes = IndexedSeq(MoveCamera) ++ (0 to 3).map(EditControlPoint) ++
    IndexedSeq(EditThickness(isHead = true), EditThickness(isHead = false), ScaleLetter, TranslateLetter, ScaleTotalThickness, BendCurve)

  val modeButtonPairs = modes.zipWithIndex.map{ case (m, id) =>
    val (name,explain) = m match {
      case MoveCamera => ("MoveCamera", "Move the camera to view different parts")
      case EditControlPoint(i) => (s"Edit P$i", "Edit this control point's position of current selected segment")
      case EditThickness(isHead) => (s"${if(isHead) "Head" else "Tail"} Thickness", "Scale the thickness of current selected segment")
      case ScaleLetter => ("Scale", "Scale size of the whole glyph")
      case TranslateLetter => ("Translate", "Shift the position of all segments")
      case ScaleTotalThickness => ("Scale Thickness", "Scale the thickness of all segments")
      case BendCurve => ("Bend Curve", "Drag mouse to bend the shape of currently selected segment")
    }
    val hotkey = if(id == 0) "`" else id.toString
    val b = new JRadioButton(name){
      setFocusable(false)
      MyButton.addAction(this, ()=>changeMode(m))
      setToolTipText(s"$name:\n$explain.\nHot key [$hotkey]")
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

  val newEditingButton = makeButton("New") { editor.newEditing() }

  val letterTypeBox = new JComboBox[MuseCharType.Value](MuseCharType.values.toArray) { setFocusable(false) }
  letterTypeBox.addItemListener(new ItemListener {
    override def itemStateChanged(e: ItemEvent): Unit = {
      val t = MuseCharType(letterTypeBox.getSelectedIndex)
      editor.changeLetterType(t)
    }
  })

  setupLayout()

  def setupLayout(): Unit ={
    val contentPanel = new JPanel{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    }

    this.add(contentPanel)

    def addRow(components: JComponent*): JPanel ={
      val panel: JPanel = new JPanel() {
        setLayout(new FlowLayout())
        components.foreach(this.add)
      }
      contentPanel.add(panel)
      panel
    }

    addRow(modeButtonPairs.map(_._2) :_*).setPreferredSize(new Dimension(400,130))

    addRow(alignTangentsCheckbox, strokeBreakCheckbox)

    addRow(appendButton, cutSegmentButton, deleteButton)

    addRow(undoButton, redoButton)

    addRow(newEditingButton, saveButton, loadButton)

    addRow(letterTypeBox)

    contentPanel.add(segmentsPanel)
  }

  def setLetterType(t: MuseCharType.Value): Unit = {
    if(t.id != letterTypeBox.getSelectedIndex){
      letterTypeBox.setSelectedIndex(t.id)
    }
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
        setLetterType(letter.letterType)
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
  import java.nio.file.Paths
  def mkFileChooser = {
    val defaultPath = Paths.get("letters").toAbsolutePath.toFile
    new JFileChooser(defaultPath){
      setFileFilter(fontFileFilter)
      setMultiSelectionEnabled(false)
    }
  }

  def saveEditing(): Unit = {
    val fc = mkFileChooser
    fc.showSaveDialog(this) match {
      case JFileChooser.APPROVE_OPTION =>
        val file = fc.getSelectedFile
        val select = file.getAbsolutePath
        val name = file.getName
        val pathToSave = if(editor.currentEditing().letter.isUppercase && !name.startsWith("upper_")){
          file.getParent + s"/upper_${name.map{_.toLower}}"
        } else select

        EditingSaver.saveToFile(pathToSave, editor.currentEditing())
      case _ => ()
    }
  }

  def loadEditing(): Unit = {
    val fc = mkFileChooser
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

  def getButtonByMode(editMode: EditMode) = {
    modeButtonPairs.collect{case (m, b) if m==editMode => b}.head
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
        case VK_G =>
          getButtonByMode(TranslateLetter).doClick()
        case VK_S =>
          getButtonByMode(ScaleLetter).doClick()
        case VK_H =>
          getButtonByMode(EditThickness(isHead = true)).doClick()
        case VK_T =>
          getButtonByMode(EditThickness(isHead = false)).doClick()
        case _ => ()
      }
      val keyId = e.getKeyCode - VK_1 + 1
      if(keyId>=1 && keyId<=9)
        modeButtonPairs(keyId)._2.doClick()
    }
  }
}



