package main

@SerialVersionUID(507692375361807682L)
case class Letter (segs: IndexedSeq[LetterSeg], startX: Double, endX: Double) {

  def getCurves(indices: Seq[Int]) = indices.map(segs)

  def width = endX - startX
}

object Letter{
  val empty = create(IndexedSeq())

  val calculationPointsPerUnit = 50

  val minWidth = 0.01

  def create(segs: IndexedSeq[LetterSeg]): Letter = {
    var startX, endX = 0.0
    def updateX(x: Double): Unit = {
      if(x<startX) startX = x
      if(x>endX) endX = x
    }
    segs.foreach{s =>
      val samples = (s.curve.controlLineLength * calculationPointsPerUnit).toInt + 1
      (0 until samples).foreach {t =>
        updateX (s.curve.eval(t).x)
      }
    }

    Letter(segs, startX, endX)
  }
}
