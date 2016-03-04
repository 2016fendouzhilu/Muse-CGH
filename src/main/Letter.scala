package main

@SerialVersionUID(507692375361807682L)
case class Letter (segs: IndexedSeq[LetterSeg]) {

  lazy val (startX, endX) = Letter.calculateEndXs(segs)

  def getCurves(indices: Seq[Int]) = indices.map(segs)

  def width = endX - startX
}

object Letter{
  val empty = create(IndexedSeq())

  val calculationPointsPerUnit = 50

  val minWidth = 0.01

  def calculateEndXs(segs: IndexedSeq[LetterSeg]) = {
    var startX, endX = 0.0
    def updateX(x: Double): Unit = {
      if(x<startX) startX = x
      if(x>endX) endX = x
    }
    segs.foreach{s =>
      s.curve.samples(calculationPointsPerUnit).foreach(v => updateX(v.x))
    }
    (startX, endX)
  }

  def create(segs: IndexedSeq[LetterSeg]): Letter = {
    Letter(segs)
  }
}
