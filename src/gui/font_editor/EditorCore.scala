package gui.font_editor

import main.{LetterSeg, MuseCharType}
import utilities.{ChangeSource, CollectionOp, CubicCurve, Vec2}

/**
  * Model for font editor
  */
class EditorCore(private var buffer: Editing) extends ChangeSource {

  private val history = new EditingHistory(buffer){
    addHistory(buffer)
  }

  private var _mode: EditMode = MoveCamera
  def mode = _mode

  def isTangentAligned: Option[Boolean] = {
    buffer.selectedInkCurves.headOption.map{_.alignTangent}
  }

  def setAlignTangent(a: Boolean) = {
    setInkCurveAttributes(isAlignTangent = true)(a)
  }

  def isStrokeBreak: Option[Boolean] = {
    buffer.selectedInkCurves.headOption.map{_.isStrokeBreak}
  }

  private def setInkCurveAttributes(isAlignTangent: Boolean)(value: Boolean): Unit = {
    val l = buffer.letter
    val newSegs = CollectionOp.transformSelected(l.segs, buffer.selects.toSet){ seg =>
      if(isAlignTangent)
        seg.copy(alignTangent = value)
      else
        seg.copy(isStrokeBreak = value)
    }
    editAndRecord(buffer.copy(letter = l.copy(segs = newSegs)))
  }

  def setStrokeBreak(s: Boolean) = setInkCurveAttributes(isAlignTangent = false)(s)

  def dragZero(): Unit = mode match {
    case EditControlPoint(id) =>
      dragControlPoint(id, Vec2.zero)
    case _ => ()
  }


  def currentEditing() = buffer

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

  def newEditing(): Unit = {
    editAndRecord(Editing.empty)
  }

  def cutSegment(sIndex: Int): Unit ={
    val letter = buffer.letter
    val segs = letter.segs
    val segsToInsert = {
      val old = segs(sIndex)
      val oldCurve = old.curve
      val mid = oldCurve.eval(0.5)
      val l = oldCurve.copy(p3 = mid, p2 = oldCurve.p1)
      val r = oldCurve.copy(p0 = mid, p1 = oldCurve.p2)
      val midWidth = (old.startWidth + old.endWidth)/2
      IndexedSeq(
        old.copy(curve = l, endWidth = midWidth),
        old.copy(curve = r, startWidth = midWidth)
      )
    }
    val newSegs = CollectionOp.modifyInsert(segs, sIndex)(segsToInsert)
    editAndRecord(buffer.copy(letter = letter.copy(segs = newSegs)))
  }

  def deleteSegment(sIndex: Int): Unit = {
    val newSegs = CollectionOp.modifyInsert(buffer.letter.segs, sIndex)(IndexedSeq())
    val selects = if (newSegs.isEmpty) Seq() else Seq(0)
    editAndRecord(buffer.copy(letter = buffer.letter.copy(segs = newSegs), selects = selects))
  }

  def appendSegment(): Unit = {
    val segs = buffer.letter.segs
    val newSeg = segs.lastOption match {
      case Some(last) =>
        val curve: CubicCurve = last.curve
        val offset = curve.p3 - curve.p0
        val delta = curve.p3-curve.p2
        val tc = curve.translate(offset)
        val newCurve = tc.copy(p1 = tc.p0 + delta)
        last.copy(curve = newCurve, startWidth = last.endWidth, endWidth = last.startWidth)
      case None => LetterSeg.initCurve
    }
    editAndRecord(buffer.copy(letter = buffer.letter.copy(segs = segs :+ newSeg), selects = Seq(segs.length)))
  }

  def changeLetterType(t: MuseCharType.Value): Unit = {
    val l = buffer.letter
    if(l.letterType != t)
      editAndRecord(buffer.copy(letter = l.copy(letterType = t)))
  }

  def dragControlPoint(pid: Int, drag: Vec2): Unit = {
    val Editing(letter, selects) = currentEditing()
    selects.headOption.foreach { segIndex =>
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

      val tControlOpt = pid match {
        case 0 => Some(1)
        case 3 => Some(2)
        case _ => None
      }
      tControlOpt.foreach { tid =>
        movePoint(segIndex, tid, drag)
      }

      val endIndex: Int = segArray.length // stroke end

      pid match {
        case 0 =>
          val nearby = segIndex - 1
          if (nearby >= 0 && segArray(nearby).connectNext) {
            setPoint(nearby, 3, newEndpoint)
          }
        case 3 =>
          val nearby = segIndex + 1
          if (nearby < endIndex && segArray(segIndex).connectNext) {
            setPoint(nearby, 0, newEndpoint)
          }
        case _ => ()
      }

    {
      // Align Tangents
      val c = segArray(segIndex).curve
      if (pid < 2) {
        val nearby = segIndex - 1
        if (nearby >= 0) {
          val nearbySeg = segArray(nearby)
          if (nearbySeg.alignTangent) {
            val tangent = (c.p0 - c.p1).normalized
            val nearLen = (nearbySeg.curve.p3 - nearbySeg.curve.p2).length
            setPoint(nearby, 2, nearbySeg.curve.p3 + tangent * nearLen)
          }
        }
      } else {
        val nearby = segIndex + 1
        if (nearby < endIndex) {
          val nearbyC = segArray(nearby).curve
          if (segArray(segIndex).alignTangent) {
            val tangent = (c.p3 - c.p2).normalized
            val nearLen = (nearbyC.p0 - nearbyC.p1).length
            setPoint(nearby, 1, nearbyC.p0 + tangent * nearLen)
          }
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

  def redo(): Unit = {
    history.redo().foreach{ e =>
      buffer = e
      notifyListeners()
    }
  }

  def scaleThickness(isHead: Boolean, ratio: Double): Unit = {
    def letter = buffer.letter
    val segArray = letter.segs.toArray
    currentEditing().selects.foreach{ segIndex =>
      val seg = segArray(segIndex)
      if(isHead){
        val t = math.max(seg.startWidth*ratio, LetterSeg.minimalWidth)
        segArray(segIndex) = seg.copy(startWidth = t)
        val nearIndex = segIndex - 1
        if(nearIndex>=0 && segArray(nearIndex).connectNext)
          segArray(nearIndex) = segArray(nearIndex).copy(endWidth = t)
      }
      else{
        val t = math.max(seg.endWidth*ratio, LetterSeg.minimalWidth)
        segArray(segIndex) = seg.copy(endWidth = t)
        val nearIndex = segIndex + 1
        if(nearIndex<segArray.length && segArray(segIndex).connectNext)
          segArray(nearIndex) = segArray(nearIndex).copy(startWidth = t)
      }
    }
    editWithoutRecord(buffer.copy(letter = letter.copy(segs = segArray.toIndexedSeq)))
  }

  def editLetterSegs(f: LetterSeg => LetterSeg): Unit = {
    def letter = buffer.letter
    val newSegs = letter.segs.map(f)
    editWithoutRecord(buffer.copy(letter = letter.copy(segs = newSegs)))
  }

  def scaleLetter(ratio: Double): Unit =
    editLetterSegs {s => s.copy(curve = s.curve.pointsMap(_ * ratio))}

  def scaleTotalThickness(ratio: Double): Unit =
    editLetterSegs {s => s.copy(startWidth = s.startWidth*ratio, endWidth = s.endWidth*ratio)}

  def translateLetter(offset: Vec2): Unit =
    editLetterSegs {s => s.copy(curve = s.curve.translate(offset))}

}
