package main

import utilities.{Vec2, CollectionOp}

object LetterType extends Enumeration {
  val LowerCase, Uppercase, PunctuationMark = Value
}


case class Letter (segs: IndexedSeq[LetterSeg], letterType: LetterType.Value) {

  lazy val (startX, endX, centerOfMass) = Letter.calculateCurveInfo(segs)

  def getCurves(indices: Seq[Int]) = indices.map(segs)

  def width = endX - startX

  lazy val (mainSegs, secondarySegs) = letterType match {
    case LetterType.LowerCase =>
      CollectionOp.firstIndex(segs)(_.isStrokeBreak) match{
        case Some(i) => segs.splitAt(i+1)
        case None => (segs, IndexedSeq())
      }
    case _ => (segs, IndexedSeq())
  }

  def isPunctuationMark = {
    letterType == LetterType.PunctuationMark
  }

  def isLowercase = letterType == LetterType.LowerCase
  
  def isUppercase = {
    letterType == LetterType.Uppercase
  }


  def modifyByAngularFunc(f: Double => Double) = {
    def modifyPoint(p: Vec2) = {
      val relative = p-centerOfMass
      val angle = math.atan2(relative.x, relative.y)
      relative * f(angle) + centerOfMass
    }

    val newSegs = segs.map { s =>
      val p0 = modifyPoint(s.curve.p0)
      val p3 = modifyPoint(s.curve.p3)
      s.dragEndpoints(p0, p3)
    }

    this.copy(segs = newSegs)
  }
}

object Letter{
  val empty = Letter(IndexedSeq(), LetterType.LowerCase)

  val calculationPointsPerUnit = 50

  val minWidth = 0.01

  def calculateCurveInfo(segs: IndexedSeq[LetterSeg]) = {
    var startX = Double.MaxValue
    var endX = Double.MinValue
    var posSum = Vec2.zero
    var sampleNum = 0
    def updateX(x: Double): Unit = {
      if(x<startX) startX = x
      if(x>endX) endX = x
    }
    segs.foreach{s =>
      s.curve.samples(calculationPointsPerUnit).foreach{v =>
        updateX(v.x)
        posSum += v
        sampleNum += 1
      }
    }
    (startX, endX, posSum/sampleNum)
  }
}
