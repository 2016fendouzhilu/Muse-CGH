package main

import utilities.RNG._
import utilities.{CubicCurve, PeriodicAngularFunctions, RNG, Vec2}

import scala.collection.mutable

/**
  * Render the letters
  */
class MuseCharRenderer(letterSpacing: Double, spaceWidth: Double, symbolFrontSpace: Double) {

  def connectionWidth(start: Double, end: Double)(t: Double) = {
    val tMin = 0.6
    def f(x: Double) = math.sqrt(x)
    val minWidth = math.min(start, end) * 0.4
    if(t<tMin) start + f(t/tMin) * (minWidth-start)
    else minWidth + f((t-tMin)/(1-tMin)) * (end-minWidth)
  }

  def renderAWord(lean: Double, letters: IndexedSeq[MuseChar]): RenderingWord = {

    def transform(xOffset: Double)(v: Vec2) = Vec2(v.x-v.y*lean+xOffset, v.y)

    val letterNum = letters.length

    def shouldConnect(l1: MuseChar, l2: MuseChar): Boolean = {
      l1.isLowercase && l2.isLowercase
    }

    def shouldDropFirst(i: Int) = i != 0 && shouldConnect(letters(i-1), letters(i))

    def shouldModifyLast(i: Int) = i != letterNum - 1 && shouldConnect(letters(i),letters(i+1))

    def shouldPrependSpace(i: Int) =
        i != 0 && (letters(i).isPunctuationMark || letters(i-1).isPunctuationMark)


    var x = 0.0
    var mainSegs, secondarySegs = IndexedSeq[WidthCurve]()

    (0 until letterNum).foreach{ i =>
      val frontSpacing = if(shouldPrependSpace(i)) symbolFrontSpace else 0.0
      val baseX = letters(i).startX
      val ss =
        (if(shouldDropFirst(i)) letters(i).mainSegs.tail else letters(i).mainSegs).
          map{_.pointsMap(transform(x-baseX+frontSpacing))}

      val newX = x + letters(i).width + letterSpacing + frontSpacing

      mainSegs = mainSegs ++ (if(shouldModifyLast(i)){
        val thisSeg = ss.last
        val nextBaseX = letters(i+1).startX
        val nextSeg = letters(i+1).mainSegs.head.pointsMap(transform(newX-nextBaseX))
        val newLast = mergeSegs(thisSeg, nextSeg)

        ss.init.map{WidthCurve.linearThickness} :+ newLast
      } else {
        ss.map{WidthCurve.linearThickness}
      })

      secondarySegs =
        secondarySegs ++ letters(i).secondarySegs.
          map{_.pointsMap(transform(x-baseX+frontSpacing))}.
          map{ WidthCurve.linearThickness}

      x = newX
    }

    RenderingWord(mainSegs, secondarySegs, x)
  }

  def mergeSegs(s1: LetterSeg, s2: LetterSeg): WidthCurve = {
    val thisCurve = s1.curve
    val nextCurve = s2.curve
    val startTangent = thisCurve.p1 - thisCurve.p0
    val endTangent = nextCurve.p2-nextCurve.p3
    val scale = (nextCurve.endPoint - thisCurve.startPoint).length /
      (thisCurve.straightLineLength + nextCurve.straightLineLength)
    val p1 = thisCurve.startPoint + startTangent * scale
    val p2 = nextCurve.endPoint + endTangent * scale

    val newCurve = CubicCurve(thisCurve.p0, p1, p2, nextCurve.p3)
    WidthCurve(newCurve, connectionWidth(s1.startWidth, s2.endWidth))
  }

  def renderAWordPlainly(lean: Double, letters: IndexedSeq[MuseChar]): RenderingWord = {
    var x = 0.0
    var segs = IndexedSeq[LetterSeg]()
    letters.foreach{ l =>
      def transform(v: Vec2) = Vec2(v.x-v.y*lean+x-l.startX, v.y)
      segs = segs ++ l.segs.map{ s => s.pointsMap(transform)}
      x += l.width + letterSpacing
    }

    val rSegs = segs.map{ WidthCurve.linearThickness }
    RenderingWord(rSegs, IndexedSeq(), x)
  }

  def renderTextInParallel(lMap: Map[Char, MuseChar], lean: Double, maxLineWidth: Double, breakWordThreshold: Double,
                           lineSpacing: Double, randomness: Double, lineRandomness: Double)(text: String): State[RNG,RenderingResult] = State((rng: RNG) => {
    require(maxLineWidth > 0 && breakWordThreshold<maxLineWidth)
    val infoBuffer = new mutable.ListBuffer[String]
    def printInfoLine(s: String): Unit = {
      infoBuffer.append(s)
//      Predef.println(s)
    }

    val (renderingElements, wordCount) = convertToTextElements(text, lMap, rng.seed, randomness, lean, printInfoLine)

    val words = new mutable.ListBuffer[(Vec2, RenderingWord)]()

    import scala.util.Random

    var x, y, yOffset = 0.0
    val rand = new Random(rng.seed)
    def randomY() = {
      // Random walking with decay
      val dy = rand.nextGaussian() * lineRandomness
      yOffset = yOffset * 0.8 + dy
      y + yOffset
    }

    def goToNewLine(): Unit ={
      y += lineSpacing
      x = 0
    }

    renderingElements.foreach {
      case TextSpace =>
        val x1 = x + spaceWidth
        if(x1<maxLineWidth) x = x1
        else goToNewLine()
      case TextNewline => goToNewLine()
      case PreRenderingWord(word, letters) =>
        def renderWord(word: RenderingWord, letters: IndexedSeq[MuseChar]): Boolean = {
          if (x + word.width < maxLineWidth) {
            // keep going
            words.append((Vec2(x, randomY()), word))
            x += word.width
            true
          } else if (maxLineWidth - x < breakWordThreshold) {
            // start from new line
            y += lineSpacing
            words.append((Vec2(0, randomY()), word))
            x = word.width
            true
          } else {
            // break word
            lMap.get('-').foreach{ hyphen =>
              val spaceLeft = maxLineWidth - x
              tryBreakWord(lean, hyphen)(letters, spaceLeft).foreach{
                case (l, r) =>
                  words.append((Vec2(x,randomY()), l))
                  y += lineSpacing
                  x = 0
                  return renderWord(r.rWord, r.letters)
              }
            }
            false
          }
        }
        val succeed = renderWord(word, letters)
        if(!succeed){
          y += lineSpacing
          x = 0
          printInfoLine("Failed to break word, ignore it.")
        }
    }

    (RenderingResult(words.toIndexedSeq, maxLineWidth, y, infoBuffer.mkString("\n")), RNG(rng.seed+wordCount))
  })

  def convertLetters(s: String, lMap: Map[Char, MuseChar]): (IndexedSeq[MuseChar], IndexedSeq[Char]) = {
    val ls = new mutable.ListBuffer[MuseChar]
    val unConverted = new mutable.ListBuffer[Char]

    s.foreach{ c =>
      lMap.get(c) match{
        case Some(l) => ls.append(l)
        case None => unConverted.append(c)
      }
    }

    (ls.toIndexedSeq, unConverted.toIndexedSeq)
  }

  def convertToTextElements(text: String, lMap: Map[Char,MuseChar], randomSeed: Long,
                            randomness: Double, lean: Double, printInfoLine: String => Unit) = {
    val newline = '\n'
    val whitespace = ' '

    val textElements: IndexedSeq[TextElement] = text.split(newline).toIndexedSeq.flatMap {
      case "" => IndexedSeq(TextNewline)
      case p =>
        p.split(whitespace).toIndexedSeq.flatMap {
          case "" => IndexedSeq(TextSpace)
          case w =>
            val (letters, unConverted) = convertLetters(w, lMap)
            if(unConverted.nonEmpty){
              val unConvertedList = unConverted.map{c => s"'$c'"}.mkString(", ")
              printInfoLine(s"Can't render $unConvertedList in word '$w'")
            }
            IndexedSeq(TextWord(letters), TextSpace)
        }.dropRight(1) :+ TextNewline
    }.dropRight(1)

    val wordCount = textElements.length
    val renderingElements = new Array[RenderingElement](wordCount)
    // Parallel rendering
    textElements.zipWithIndex.par.foreach { case (te, i) =>
      renderingElements(i) = te match {
        case TextWord(letters) =>
          var r = RNG(randomSeed + i)
          val randomLetters = letters.map { l =>
            val (coes, newR) = RNG.nextDoubles(6)(r)
            r = newR
            val angF = PeriodicAngularFunctions.sineWave(3, coes, randomness)
            l.modifyByAngularFunc(angF)
          }
          val w = renderAWord(lean, randomLetters)
          PreRenderingWord(w, randomLetters)
        case other: RenderingElement => other
      }
    }
    (renderingElements, wordCount)
  }


  def tryBreakWord(lean: Double, hyphen: MuseChar)(letters: IndexedSeq[MuseChar], spaceLeft: Double): Option[(RenderingWord, PreRenderingWord)] = {
    var x = hyphen.width + symbolFrontSpace * 2
    val head = letters.takeWhile{l =>
      x += l.width
      x < spaceLeft
    }
    if(head.isEmpty) None
    else{
      val tail = letters.drop(head.length)
      val firstPart = renderAWord(lean, head :+ hyphen)
      val secondPart = renderAWord(lean, tail)
      Some(firstPart, PreRenderingWord(secondPart, tail))
    }
  }

}

sealed trait RenderingElement

sealed trait TextElement

case object TextSpace extends TextElement with RenderingElement

case object TextNewline extends TextElement with RenderingElement

case class TextWord(word: IndexedSeq[MuseChar]) extends TextElement

case class PreRenderingWord(rWord: RenderingWord, letters: IndexedSeq[MuseChar]) extends RenderingElement

case class RenderingWord(mainSegs: IndexedSeq[WidthCurve], secondarySegs: IndexedSeq[WidthCurve], width: Double)

case class RenderingResult(words: IndexedSeq[(Vec2, RenderingWord)], lineWidth: Double, height: Double, info: String)