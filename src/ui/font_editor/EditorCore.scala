package ui.font_editor

import main.{LetterSeg, MuseCharType}
import utilities.MyMath.MinimizationConfig
import utilities._

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

//  def dragZero(): Unit = mode match {
//    case EditControlPoint(id) =>
//      dragControlPoint(id, Vec2.zero)
//    case _ => ()
//  }


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

  def selectSegments(indices: Seq[Int]): Unit = {
    editAndRecord(buffer.copy(selects = indices.toList))
  }

  def changeMode(m: EditMode): Unit ={
    _mode = m
    notifyListeners()
  }

  def newEditing(): Unit = {
    editAndRecord(Editing.empty)
  }

  def cutSegment(sIndex: Int, samplePoints: Int = 20): Unit ={
    val letter = buffer.letter
    val segs = letter.segs
    val segsToInsert = {
      val old = segs(sIndex)
      val oldCurve = old.curve
      val curveSamples = oldCurve.samples(samplePoints*2-1)
      val firstHalf = curveSamples.take(samplePoints)
      val secondHalf = curveSamples.drop(samplePoints-1)

      val defaultConfig: MinimizationConfig = BendCurveBuffer.defaultConfig
      val lConfig = defaultConfig.copy(errorForStop = defaultConfig.errorForStop * MyMath.totalLength(firstHalf))
      val rConfig = defaultConfig.copy(errorForStop = defaultConfig.errorForStop * MyMath.totalLength(secondHalf))
      val (r1, l) = CubicCurve.dotsToCurve(curveSampleNum = samplePoints*2, lConfig)(firstHalf)
      val (r2, r) = CubicCurve.dotsToCurve(curveSampleNum = samplePoints*2, rConfig)(secondHalf)
      List(r1,r2).foreach(println)

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
        val dir = curve.evalTangentDirection(1.0)
        val newSegLen = 0.1
        val interpolation = MyMath.linearInterpolate(curve.p3, curve.p3+dir*newSegLen)_
        val newCurve = curve.copy(interpolation(0), interpolation(0.3), interpolation(0.7), interpolation(1))
        last.copy(curve = newCurve, startWidth = last.endWidth, endWidth = last.endWidth)
      case None => LetterSeg.initCurve
    }
    editAndRecord(buffer.copy(letter = buffer.letter.copy(segs = segs :+ newSeg), selects = Seq(segs.length)))
  }

  def changeLetterType(t: MuseCharType.Value): Unit = {
    val l = buffer.letter
    if(l.letterType != t)
      editAndRecord(buffer.copy(letter = l.copy(letterType = t)))
  }

  def dragEndOrTangentPoint(segs: IndexedSeq[LetterSeg], selectedId: Int, dragEnd: Boolean, dragHead: Boolean, drag: Vec2): IndexedSeq[LetterSeg] = {
    val endPointId = if (dragHead) 0 else 3
    val tanPointId = if (dragHead) 1 else 2
    val endIndex = segs.length

    val candidate = if (dragHead) selectedId - 1 else selectedId + 1

    val newSelectedSeg =
      if (dragEnd)
        segs(selectedId).dragPoint(endPointId, drag).dragPoint(tanPointId, drag)
      else
        segs(selectedId).dragPoint(tanPointId, drag)

    val update1 = segs.updated(selectedId, newSelectedSeg)

    if (candidate >= 0 && candidate < endIndex) {
      val nearSeg = segs(candidate)
      if (dragEnd) {
        if (dragHead && nearSeg.connectNext || (!dragHead && newSelectedSeg.connectNext)) {
          val newSeg = segs(candidate).dragPoint(3 - endPointId, drag).dragPoint(3 - tanPointId, drag)
          return update1.updated(candidate, newSeg)
        }
      } else {
        if (dragHead && nearSeg.alignTangent || (!dragHead && newSelectedSeg.alignTangent)) {
          val dir = (newSelectedSeg.curve.getPoint(endPointId) - newSelectedSeg.curve.getPoint(tanPointId)).normalized
          val nearCurve = segs(candidate).curve
          val oldTangentLen = (nearCurve.getPoint(3 - tanPointId) - nearCurve.getPoint(3 - endPointId)).length
          val newSeg = segs(candidate).setPoint(3 - tanPointId, nearCurve.getPoint(3 - endPointId) + dir * oldTangentLen)
          return update1.updated(candidate, newSeg)
        }
      }
    }

    update1
  }


  def dragControlPoint(pid: Int, drag: Vec2): Unit = {
    val Editing(letter, selects) = currentEditing()
    selects.headOption.foreach { segIndex =>
      val newSegs = if (pid == 0 || pid == 3) {
        dragEndOrTangentPoint(letter.segs, segIndex, dragEnd = true, dragHead = pid == 0, drag)
      } else {
        dragEndOrTangentPoint(letter.segs, segIndex, dragEnd = false, dragHead = pid == 1, drag)
      }
      editWithoutRecord(buffer.copy(letter = letter.copy(segs = newSegs)))
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
