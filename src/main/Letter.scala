package main

case class Letter(segs: IndexedSeq[InkCurve], cutIndices: IndexedSeq[Int], width: Double, tall: Double, deep: Double){
  def height = tall + deep

  def getCurves(indices: Seq[Int]) = indices.map(segs)
}

object Letter{
  val empty = Letter(IndexedSeq(), IndexedSeq(), 2.0, 2.0, 2.0)
  val smallA = Letter(IndexedSeq(), IndexedSeq(), 2.0, 2.0, 2.0)
}
