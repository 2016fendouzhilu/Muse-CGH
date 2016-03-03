package main

case class Letter(segs: IndexedSeq[LetterSeg], width: Double, tall: Double, deep: Double){
  def height = tall + deep

  def getCurves(indices: Seq[Int]) = indices.map(segs)
}

object Letter{
  val empty = Letter(IndexedSeq(), 2.0, 2.0, 2.0)
  val smallA = Letter(IndexedSeq(), 2.0, 2.0, 2.0)
}
