package render

import main.{Letter, LetterSeg}
import utilities.{MyMath, CubicCurve, Vec2}

/**
  * Render the letters
  */
class LetterRenderer(letterSpacing: Double) {

  def connectionWidth(start: Double, end: Double)(t: Double) = {
    val tMin = 0.6
    val minWidth = math.min(start, end) * 0.3
    if(t<tMin) start + t/tMin * (minWidth-start)
    else minWidth + (t-tMin)/(1-tMin) * (end-minWidth)
  }

  def renderAWord(offset: Vec2, lean: Double, letters: IndexedSeq[Letter]): RenderingResult = {

    def transform(xOffset: Double)(v: Vec2) = Vec2(v.x-v.y*lean+xOffset, v.y) + offset

    val letterNum = letters.length

    def shouldDropFirst(i: Int) = i != 0

    def shouldModifyLast(i: Int) = i != letterNum - 1

    var x = 0.0
    val rSegs = (0 until letterNum).flatMap{ i =>
      val baseX = letters(i).startX
      val ss =
        (if(shouldDropFirst(i)) letters(i).mainSegs.tail else letters(i).mainSegs).map{_.pointsMap(transform(x-baseX))}

      val newX = x + letters(i).width + letterSpacing

      val mainSegs = if(shouldModifyLast(i)){
        val thisSeg = ss.last
        val nextBaseX = letters(i+1).startX
        val nextSeg = letters(i+1).mainSegs.head.pointsMap(transform(newX-nextBaseX))
        val newLast = mergeSegs(thisSeg, nextSeg)

        ss.init.map{RenderingSeg.linearThickness} :+ newLast
      } else {
        ss.map{RenderingSeg.linearThickness}
      }

      val secondarySegs = letters(i).secondarySegs.map{_.pointsMap(transform(x-baseX))}

      x = newX
      mainSegs ++ secondarySegs.map{ RenderingSeg.linearThickness}
    }

    RenderingResult(rSegs)
  }

  def mergeSegs(s1: LetterSeg, s2: LetterSeg): RenderingSeg = {
    val thisCurve = s1.curve
    val nextCurve = s2.curve
    val startTangent = thisCurve.p1 - thisCurve.p0
    val endTangent = nextCurve.p2-nextCurve.p3
    val scale = ((nextCurve.endPoint-thisCurve.startPoint).length /
      (thisCurve.straightLineLength +nextCurve.straightLineLength))
    val p1 = thisCurve.startPoint + startTangent * scale
    val p2 = nextCurve.endPoint + endTangent * scale

    val newCurve = CubicCurve(thisCurve.p0, p1, p2, nextCurve.p3)
    RenderingSeg(newCurve, connectionWidth(s1.startWidth, s2.endWidth))
  }

  def renderAWordPlainly(offset: Vec2, lean: Double, letters: IndexedSeq[Letter]): RenderingResult = {
    var x = 0.0
    var segs = IndexedSeq[LetterSeg]()
    letters.foreach{ l =>
      def transform(v: Vec2) = Vec2(v.x-v.y*lean+x-l.startX, v.y) + offset
      segs = segs ++ l.segs.map{ s => s.pointsMap(transform)}
      x += l.width + letterSpacing
    }

    val rSegs = segs.map{ RenderingSeg.linearThickness }
    RenderingResult(rSegs)
  }
}

case class RenderingResult(rSegs: IndexedSeq[RenderingSeg])