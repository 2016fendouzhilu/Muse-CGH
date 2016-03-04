package render

import main.{Letter, LetterSeg}
import utilities.{MyMath, CubicCurve, Vec2}

/**
  * Render the letters
  */
class LetterRenderer(letterSpacing: Double) {

  def connectionWidth(start: Double, end: Double)(t: Double) = {
    val tMin = 0.6
    val minWidth = math.min(start, end) * 0.4
    if(t<tMin) start + t/tMin * (minWidth-start)
    else minWidth + (t-tMin)/(1-tMin) * (end-minWidth)
  }

  def renderAWord(offset: Vec2, lean: Double, letters: IndexedSeq[Letter]): RenderingResult = {
    def transform(xOffset: Double)(v: Vec2) = Vec2(v.x-v.y*lean+xOffset, v.y) + offset

    var x = 0.0
    var rSegs = IndexedSeq[RenderingSeg]()

    val letterNum = letters.length

    def shouldDropFirst(i: Int) = i != 0

    def shouldModifyLast(i: Int) = i != letterNum - 1

    (0 until letterNum).foreach{ i =>
      val ss =
        (if(shouldDropFirst(i)) letters(i).mainSegs.tail else letters(i).mainSegs).map{_.pointsMap(transform(x))}

      val newX = x + letters(i).width + letterSpacing

      rSegs = rSegs ++ (if(shouldModifyLast(i)){
        val nextSeg = letters(i+1).mainSegs.head
        val nextCurve = nextSeg.curve
        val thisCurve = ss.last.curve
        val startTangent = thisCurve.p1 - thisCurve.p0
        val scale = math.sqrt((nextCurve.p3-thisCurve.p0).length / thisCurve.controlLineLength)
        val p1 = thisCurve.p0 + startTangent * scale
        val p3 = transform(newX)(nextCurve.p3)
        val endTangent = nextCurve.p2-nextCurve.p3
        val p2 = p3 + endTangent * scale
        val newLast = ss.last match{
          case seg@LetterSeg(c, sw, ew, _, _) =>
            val curve = CubicCurve(c.p0, p1, p2, p3)
            RenderingSeg(curve, connectionWidth(sw, nextSeg.endWidth))
        }
        ss.init.map{RenderingSeg.linearThickness} :+ newLast
      } else {
        ss.map{RenderingSeg.linearThickness}
      })

      val secondarySegs = letters(i).secondarySegs.map{_.pointsMap(transform(x))}
      rSegs = rSegs ++ secondarySegs.map{ RenderingSeg.linearThickness}

      x = newX
    }

    RenderingResult(rSegs)
  }

  def renderAWordPlainly(offset: Vec2, lean: Double, letters: IndexedSeq[Letter]): RenderingResult = {
    var x = 0.0
    var segs = IndexedSeq[LetterSeg]()
    letters.foreach{ l =>
      def transform(v: Vec2) = Vec2(v.x-v.y*lean+x, v.y) + offset
      segs = segs ++ l.segs.map{ s => s.pointsMap(transform)}
      x += l.width + letterSpacing
    }

    val rSegs = segs.map{ RenderingSeg.linearThickness }
    RenderingResult(rSegs)
  }
}

case class RenderingResult(rSegs: IndexedSeq[RenderingSeg])