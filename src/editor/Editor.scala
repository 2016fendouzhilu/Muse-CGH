package editor

import main.{InkCurve, Letter}
import mymath.{MyMath, Vec2}

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
    editAndRecord(buffer.copy(selects = indexOp.toList))
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

      val endIndex: Int = segArray.length // stroke end

      if(connectNearby){
        pid match {
          case 0 =>
            val nearby = segIndex - 1
            if(nearby>=0){
              setPoint(nearby, 3, newEndpoint)
            }
          case 3 =>
            val nearby = segIndex + 1
            if(nearby<endIndex){
              setPoint(nearby, 0, newEndpoint)
            }
          case _ => ()
        }
      }

      if(alignTangents){
        val c = segArray(segIndex).curve
        if(pid<2){
          val nearby = segIndex-1
          if(nearby>=0){
            val nearbyC = segArray(nearby).curve
            val relative = c.p0 - c.p1
            setPoint(nearby, 2, nearbyC.p3+relative)
          }
        }else{
          val nearby = segIndex+1
          if(nearby<endIndex){
            val nearbyC = segArray(nearby).curve
            val relative = c.p3 - c.p2
            setPoint(nearby, 1, nearbyC.p0+relative)
          }
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

  def scaleThickness(isHead: Boolean, ratio: Double): Unit = {
    val letter = currentEditing().letter
    val segArray = letter.segs.toArray
    currentEditing().selects.foreach{ segIndex =>
      val seg = segArray(segIndex)
      if(isHead){
        val t = math.max(seg.startWidth*ratio, InkCurve.minimalWidth)
        segArray(segIndex) = seg.copy(startWidth = t)
        val nearIndex = segIndex - 1
        if(connectNearby && nearIndex>=0)
          segArray(nearIndex) = segArray(nearIndex).copy(endWidth = t)
      }
      else{
        val t = math.max(seg.endWidth*ratio, InkCurve.minimalWidth)
        segArray(segIndex) = seg.copy(endWidth = t)
        val nearIndex = segIndex + 1
        if(connectNearby && nearIndex<segArray.length)
          segArray(nearIndex) = segArray(nearIndex).copy(startWidth = t)
      }
    }
    editWithoutRecord(buffer.copy(letter = letter.copy(segs = segArray.toIndexedSeq)))
  }
}

trait EditorListener {
  def editingUpdated(): Unit
}

case class Editing(letter: Letter, selects: Seq[Int]){
  def selectedInkCurves = letter.getCurves(selects)
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