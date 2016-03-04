package render

import main.{Letter, LetterSeg}
import utilities.Vec2

/**
  * Render the letters
  */
class LetterRenderer(letterSpacing: Double) {
  def renderAWord(offset: Vec2, lean: Double, letters: IndexedSeq[Letter]): RenderingResult = {
    var x = 0.0
    var segs = IndexedSeq[LetterSeg]()
    letters.foreach{ l =>
      def transform(v: Vec2) = Vec2(v.x-v.y*lean+x, v.y)
      segs = segs ++ l.segs.map{ s => s.pointsMap(transform)}
      x += l.width + letterSpacing
    }

    RenderingResult(segs)
  }
}

case class RenderingResult(segs: IndexedSeq[LetterSeg])