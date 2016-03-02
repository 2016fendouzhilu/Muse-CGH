package editor

import main.Letter
import mymath.Vec2

/**
  * Created by weijiayi on 2/29/16.
  */
class Editor(private var buffer: Editing) {
  private val listeners =  scala.collection.mutable.ListBuffer[EditorListener]()

  private val history = new EditingHistory(buffer){
    addHistory(buffer)
  }

  private var _mode: EditMode = MoveCamera
  def mode = _mode

  var _alignTangents: Boolean = false
  def alignTangents: Boolean = _alignTangents

  def alignTangents_=(value: Boolean): Unit = {
    _alignTangents = value
    dragZero()
  }

  var _connectNearby: Boolean = false
  def connectNearby: Boolean = _connectNearby

  def connectNearby_=(value: Boolean): Unit = {
    _connectNearby = value
    dragZero()
  }

  def dragZero(): Unit = mode match {
    case EditControlPoint(id) =>
      dragControlPoint(id, Vec2.zero)
    case _ => ()
  }

  def addListener(l: EditorListener) = {
    listeners += l
    l.editingUpdated()
  }

  def currentEditing() = buffer

  def notifyListeners() = listeners.foreach(_.editingUpdated())

  def editAndRecord(editing: Editing): Unit ={
    editWithoutRecord(editing)
    recordNow()
  }

  def recordNow(): Unit = {
    history.addHistory(buffer)
    println(s"new history: $buffer")
  }

  def editWithoutRecord(editing: Editing): Unit ={
    buffer = editing
    notifyListeners()
  }

  def selectSegment(indexOp: Option[Int]): Unit = {
    editAndRecord(buffer.newSelected(indexOp.toList))
  }

  def changeMode(m: EditMode): Unit ={
    _mode = m
    notifyListeners()
  }

  def insertSegment(index: Int): Unit = index match{
    case 0 =>
//      assert(currentEditing.letter)
  }

  def dragControlPoint(pid: Int, drag: Vec2): Unit ={
    val Editing(letter, selects) = currentEditing()
    selects.headOption.foreach{segIndex =>
      val segArray = letter.segs.toArray

      def setPoint(segIndex: Int, pid: Int, newP: Vec2) = {
        segArray(segIndex) = segArray(segIndex).setPoint(pid, newP)
      }

      def movePoint(segIndex: Int, pid: Int, offset: Vec2): Vec2 = {
        val selectedInk = segArray(segIndex)
        val newP = selectedInk.curve.getPoint(pid) + offset
        setPoint(segIndex, pid, newP)
        newP
      }

      val newEndpoint = movePoint(segIndex, pid, drag)

      val tControlOpt = pid match{
        case 0 => Some(1)
        case 3 => Some(2)
        case _ => None
      }
      tControlOpt.foreach{tid =>
        movePoint(segIndex, tid, drag)
      }

      if(connectNearby){
        pid match {
          case 0 =>
            val nearby = segIndex - 1
            if(nearby>=0){
              setPoint(nearby, 3, newEndpoint)
            }
          case 3 =>
            val nearby = segIndex + 1
            if(nearby<segArray.length){
              setPoint(nearby, 0, newEndpoint)
            }
          case _ => ()
        }
      }

      val newLetter = letter.copy(segs = segArray.toIndexedSeq)
      editWithoutRecord(buffer.copy(letter = newLetter))
    }
  }

  def undo(): Unit = {
    buffer = history.undo()
    notifyListeners()
  }
}

trait EditorListener {
  def editingUpdated(): Unit
}

case class Editing(letter: Letter, selects: Seq[Int]){
  def newSelected(i: Seq[Int]) = Editing(letter, i)

  def newLetter(l: Letter) = Editing(l, selects)
}

class EditingHistory(init: Editing) {
  private var history: List[Editing] = List(init)

  def addHistory(e: Editing): Unit ={
    history = e :: history
  }

  def undo(): Editing = history match{
    case now::last::_ =>
      history = history.tail
      last
    case _ => history.head
  }
}