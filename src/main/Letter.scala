package main

@SerialVersionUID(507692375361807682L)
class Letter private (val segs: IndexedSeq[LetterSeg], val startX: Double, val endX: Double) extends Serializable {

  def getCurves(indices: Seq[Int]) = indices.map(segs)

  def width = endX - startX

  override def toString: String = s"Letter($segs,$startX,$endX)"

}

object Letter{
  val empty = Letter(IndexedSeq())
  val smallA = Letter(IndexedSeq())

  val calculationPointsPerUnit = 50

  val minWidth = 0.01

  def apply(segs: IndexedSeq[LetterSeg]): Letter = {
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

    new Letter(segs, startX, endX)
  }

  def unapply(l: Letter) = Some(l.segs, l.startX, l.endX)

  def create(segs: IndexedSeq[LetterSeg], startX: Double, endX: Double) = new Letter(segs, startX, endX)
}
