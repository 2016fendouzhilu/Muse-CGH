package render

import main.{Letter, LetterSeg}
import utilities.{MyMath, CubicCurve, Vec2}
import collection.mutable


/**
  * Render the letters
  */
class LetterRenderer(letterSpacing: Double, spaceWidth: Double) {

  def connectionWidth(start: Double, end: Double)(t: Double) = {
    val tMin = 0.6
    def f(x: Double) = math.sqrt(x)
    val minWidth = math.min(start, end) * 0.4
    if(t<tMin) start + f(t/tMin) * (minWidth-start)
    else minWidth + f((t-tMin)/(1-tMin)) * (end-minWidth)
  }

  def renderAWord(lean: Double, letters: IndexedSeq[Letter]): RenderingWord = {

    def transform(xOffset: Double)(v: Vec2) = Vec2(v.x-v.y*lean+xOffset, v.y)

    val letterNum = letters.length

    def shouldDropFirst(i: Int) = i != 0

    def shouldModifyLast(i: Int) = i != letterNum - 1

    var x = 0.0
    var mainSegs, secondarySegs = IndexedSeq[RenderingSeg]()
    (0 until letterNum).foreach{ i =>
      val baseX = letters(i).startX
      val ss =
        (if(shouldDropFirst(i)) letters(i).mainSegs.tail else letters(i).mainSegs).map{_.pointsMap(transform(x-baseX))}

      val newX = x + letters(i).width + letterSpacing

      mainSegs = mainSegs ++ (if(shouldModifyLast(i)){
        val thisSeg = ss.last
        val nextBaseX = letters(i+1).startX
        val nextSeg = letters(i+1).mainSegs.head.pointsMap(transform(newX-nextBaseX))
        val newLast = mergeSegs(thisSeg, nextSeg)

        ss.init.map{RenderingSeg.linearThickness} :+ newLast
      } else {
        ss.map{RenderingSeg.linearThickness}
      })

      secondarySegs =
        secondarySegs ++ letters(i).secondarySegs.
          map{_.pointsMap(transform(x-baseX))}.
          map{ RenderingSeg.linearThickness}

      x = newX
    }

    RenderingWord(mainSegs, secondarySegs, x)
  }

  def mergeSegs(s1: LetterSeg, s2: LetterSeg): RenderingSeg = {
    val thisCurve = s1.curve
    val nextCurve = s2.curve
    val startTangent = thisCurve.p1 - thisCurve.p0
    val endTangent = nextCurve.p2-nextCurve.p3
    val scale = (nextCurve.endPoint - thisCurve.startPoint).length /
      (thisCurve.straightLineLength + nextCurve.straightLineLength)
    val p1 = thisCurve.startPoint + startTangent * scale
    val p2 = nextCurve.endPoint + endTangent * scale

    val newCurve = CubicCurve(thisCurve.p0, p1, p2, nextCurve.p3)
    RenderingSeg(newCurve, connectionWidth(s1.startWidth, s2.endWidth))
  }

  def renderAWordPlainly(lean: Double, letters: IndexedSeq[Letter]): RenderingWord = {
    var x = 0.0
    var segs = IndexedSeq[LetterSeg]()
    letters.foreach{ l =>
      def transform(v: Vec2) = Vec2(v.x-v.y*lean+x-l.startX, v.y)
      segs = segs ++ l.segs.map{ s => s.pointsMap(transform)}
      x += l.width + letterSpacing
    }

    val rSegs = segs.map{ RenderingSeg.linearThickness }
    RenderingWord(rSegs, IndexedSeq(), x)
  }

  def renderText(lMap: Map[Char, Letter], lean: Double, maxLineWidth: Double, breakWordThreshold: Double, lineSpacing: Double)(text: String): RenderingResult = {
    require(maxLineWidth > 0 && breakWordThreshold<maxLineWidth)
    val newline = "\n"
    val whitespace = " "
    val paragraphs = text.split(newline)

    val words = new mutable.ListBuffer[(Vec2, RenderingWord)]()

    var x, y = 0.0
    paragraphs.foreach{ p =>
      p.split(whitespace).foreach{
        case "" => x + spaceWidth
        case w =>
          val (letters, unConverted) = convertLetters(w, lMap)
          unConverted.foreach(c => println(s"can't render $c in word $w"))
          val word = renderAWord(lean, letters)
          val deltaX = word.width + spaceWidth

          if(x + word.width < maxLineWidth) {
            // keep going
            words.append((Vec2(x, y), word))
            x += deltaX
          } else if(maxLineWidth-x < breakWordThreshold) {
            // start from new line
            y += lineSpacing
            words.append((Vec2(0,y),word))
            x = deltaX
          } else{
            // break word
            println("don't know how to break yet!")
            y += lineSpacing
            x = 0
          }
      }
      y += lineSpacing
      x = 0
    }

    RenderingResult(words.toIndexedSeq, maxLineWidth, y)
  }

  def convertLetters(s: String, lMap: Map[Char, Letter]): (IndexedSeq[Letter], IndexedSeq[Char]) = {
    val ls = new mutable.ListBuffer[Letter]
    val unConverted = new mutable.ListBuffer[Char]

    s.foreach{ c =>
      lMap.get(c) match{
        case Some(l) => ls.append(l)
        case None => unConverted.append(c)
      }
    }

    (ls.toIndexedSeq, unConverted.toIndexedSeq)
  }

}


case class RenderingWord(mainSegs: IndexedSeq[RenderingSeg], secondarySegs: IndexedSeq[RenderingSeg], width: Double)

case class RenderingResult(words: IndexedSeq[(Vec2, RenderingWord)], lineWidth: Double, height: Double)