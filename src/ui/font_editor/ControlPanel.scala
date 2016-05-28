package ui.font_editor

import java.awt.event.{ItemEvent, ItemListener, KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import ui.MySwing
import ui.MySwing._
import main.MuseCharType
import ui.font_editor.ControlPanel.ModeInfo
import utilities.ValueTextComponent.KeyCode
import utilities.{ChangeListener, EditingSaver}

/**
  * Control Panel for font editor
  */

class ControlPanel(editor: EditorCore, zoomAction: Double => Unit) extends JPanel with ChangeListener{
  val segmentsPanel = new SegButtonsPanel(i => editor.selectSegment(Some(i)))

  val modes = IndexedSeq(MoveCamera) ++ (0 to 3).map(EditControlPoint) ++
    IndexedSeq(EditThickness(isHead = true), EditThickness(isHead = false), ScaleLetter, TranslateLetter, ScaleTotalThickness, BendCurve)

  val modeInfos: IndexedSeq[ModeInfo] = {
    import KeyEvent._
    modes.zipWithIndex.map{ case (m, id) =>
      val (name,explain, hks: Set[Int], hkNames: Set[String]) = m match {
        case MoveCamera => (
          "MoveCamera", "Move the camera to view different parts",
          Set(VK_BACK_QUOTE, VK_M),Set("`","m"))
        case EditControlPoint(i) => (s"Edit P$i", "Edit this control point's position of current selected segment",
          Set(),Set())
        case EditThickness(isHead) => (s"${if(isHead) "Head" else "Tail"} Thickness", "Scale the thickness of current selected segment",
          Set(if(isHead) VK_H else VK_T),Set(if(isHead) "H" else "T"))
        case BendCurve => ("Bend Curve", "Drag mouse to bend the shape of currently selected segment",
          Set(VK_B),Set("B"))
        case ScaleLetter => ("Scale", "Scale size of the whole glyph",
          Set(VK_S),Set("S"))
        case TranslateLetter => ("Translate", "Shift the position of all segments",
          Set(VK_G),Set("G"))
        case ScaleTotalThickness => ("Scale Thickness", "Scale the thickness of all segments",
          Set(),Set())
      }

      ModeInfo(id, m, name, explain, hotKeys = hks++Set(VK_0+id), hotKeyNames = hkNames ++ Set(s"$id"))
    }
  }

  def getModeInfo(mode: EditMode) = modeInfos(modes.indexOf(mode))

  val modeSelector = new JComboBox[String](modeInfos.map(_.name).toArray)

  MySwing.reactAction(modeSelector){
    val i = modeSelector.getSelectedIndex
    changeMode(modes(i))
  }

  val alignTangentsCheckbox = new JCheckBox("Align Tangents"){
    MySwing.addAction(this, () => editor.setAlignTangent(this.isSelected))
    setFocusable(false)
  }
  val strokeBreakCheckbox = new JCheckBox("Stroke Break"){
    MySwing.addAction(this, () => editor.setStrokeBreak(this.isSelected))
    setFocusable(false)
  }

  def makeButton(name: String) (action: => Unit): JButton = {
    val b = new JButton(name){ setFocusable(false) }
    MySwing.addAction(b, ()=>action)
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

    addRow(modeSelector)

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
    val modeInfo = getModeInfo(editor.mode)
    modeSelector.setSelectedIndex(modeInfo.id)
    modeSelector.setToolTipText(modeInfo.explain + s"\n (Hot Keys: ${modeInfo.hotKeyNames.map(n => s"[$n]").mkString(", ")})")
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

  def keyActionMap: Map[KeyCode, KeyEvent =>Unit] = {
    import KeyEvent._
    var map = Map[KeyCode, KeyEvent =>Unit](
      (VK_RIGHT, e => segmentsPanel.moveSelection(1) ),
      (VK_LEFT, e => segmentsPanel.moveSelection(-1) ),
      (VK_ESCAPE, e => editor.selectSegment(None) ),
      (VK_Z, e => if(e.isControlDown || e.isAltDown) undoButton.doClick() ),
      (VK_R, e => if(e.isControlDown || e.isAltDown) redoButton.doClick() ),
      (VK_UP, e => zoomAction(1.25)),
      (VK_DOWN, e => zoomAction(0.8)),
      (VK_A, e => appendButton.doClick())
    )

    for {
      info <- modeInfos
      keyCode <- info.hotKeys
    }{
      if(map.contains(keyCode)) throw new Exception(s"key code $keyCode already used!")
      map = map + (keyCode, e => changeMode(info.mode))
    }
    map
  }

  def makeKeyListener() = new KeyAdapter {
    val keyMap = keyActionMap
    override def keyReleased(e: KeyEvent): Unit = {
      keyMap(e.getKeyCode)(e)
    }
  }
}

object ControlPanel{
  case class ModeInfo(id: Int, mode: EditMode, name: String, explain: String, hotKeys: Set[Int], hotKeyNames: Set[String])
}
