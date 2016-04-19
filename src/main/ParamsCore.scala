package main

import ui.user.{EdgeSpace, PaintableResult}
import utilities.{ChangeSource, MuseCharMapLoader, RNG, Settable}
import NamedConstraint.{positiveConstraint,noConstraint}

/**
 * Stores all the user-settable parameters
 */
class ParamsCore() extends ChangeSource {
  val textToRender = newSettable[String]("")


  val samplesPerUnit = newSettable[Double](50.0)
  val pixelPerUnit = newSettable[Double](10.0)
  val lean = newSettable[Double](0.3)
  val thicknessScale = newSettable[Double](2.8)

  val fontRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(pixelPerUnit, "Pixel density", positiveConstraint,
      description =  "how many pixels per unit length, a lower case 'a' is about 1 unit high"),
    DoubleFieldInfo(samplesPerUnit, "Samples", positiveConstraint,
      description = "how many quadrilaterals are used per unit length"),
    DoubleFieldInfo(lean, "Lean", noConstraint, description = "tan value of slopping angle"),
    DoubleFieldInfo(thicknessScale, "Thickness", positiveConstraint, description = "the thickness of strokes")
  )


  val maxLineWidth = newSettable[Double](90.0)
  val breakWordThreshold = newSettable[Double](4.0)
  val aspectRatio = newSettable[Double](-1.0)
  val lineSpacing = newSettable[Double](4.0)

  val layoutRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(maxLineWidth, "Line width",
      NamedConstraint[Double](w=> w > 0 && w > breakWordThreshold.get, "must be greater than Break threshold"),
      description = "maximum line width"),
    DoubleFieldInfo(lineSpacing, "Line spacing", positiveConstraint, description = "distance between lines"),
    DoubleFieldInfo(breakWordThreshold, "Break threshold",
      NamedConstraint[Double](b => b > 0 && b < maxLineWidth.get, "must be smaller than Line width"),
      description = "maximum empty space allowed before breaking the last word in a line"),
    DoubleFieldInfo(aspectRatio, "Aspect Ratio", noConstraint,
      description = "the ratio of image height and width, use automatic aspect ratio if specified as negative")
  )


  val spaceWidth = newSettable[Double](0.8)
  val letterSpacing = newSettable[Double](0.0)
  val symbolFrontSpace = newSettable[Double](0.4)

  val wordRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(spaceWidth, "Space width", positiveConstraint,
      description = "the size in unit for a whitespace"),
    DoubleFieldInfo(letterSpacing, "Letter spacing", noConstraint,
      description = "extra spacing between letters in a word"),
    DoubleFieldInfo(symbolFrontSpace, "Mark spacing", noConstraint,
      description = "extra spacing for punctuation marks and numbers")
  )


  val randomness = newSettable[Double](0.07)
  val lineRandomness = newSettable[Double](0.1)
  val seed = newSettable[Double](0.0)

  val randomRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(randomness, "Letter Random", noConstraint, description = "how much randomness is used for letters in words"),
    DoubleFieldInfo(lineRandomness, "Line Random", noConstraint,
      description = "how much randomness is used for height perturbation of words in a line"),
    DoubleFieldInfo(seed, "Seed", NamedConstraint[Double](s => s >= -1 && s <= 1.0, "must be within -1.0 and 1.0"),
      description = "seed value used to generate random numbers")
  )


  val penSpeed = newSettable[Double](40)
  val frameRate = newSettable[Double](60)
  val wordsRestDis= newSettable[Double](5)

  val animationRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(penSpeed, "Animation Speed", positiveConstraint,
      description = "how fast the pen moves during animation"),
    DoubleFieldInfo(frameRate, "Animation FPS", positiveConstraint,
      description = "the updating frequency in animation"),
    DoubleFieldInfo(wordsRestDis, "Halt", positiveConstraint,
      description = "How long should the pen wait before writing the next word")
  )


  val leftEdge, rightEdge = newSettable[Double](2)
  val topEdge, bottomEdge = newSettable[Double](4)

  val edgeRow = (List(leftEdge, rightEdge, topEdge, bottomEdge) zip List("Left Edge", "Right Edge", "Top Edge", "Bottom Edge")).map{
    case (settable, name) => DoubleFieldInfo(settable, name, noConstraint, description = "extra distance to the edge of a page")
  }

  val interactiveMode = newSettable[Boolean](true)
  val letterMap = newSettable(MuseCharMapLoader.loadDefaultCharMap())

  /**
   * Set to 2 for Retina display, 1 for normal
   */
  val screenPixelFactor = newSettable[Int](2)

  var isAnimationMode = false

  var animationFinished = true


  def getPaintableResult(infoPrinter: String=>Unit): PaintableResult = {
    val text = textToRender.get

    val (result, _) = {
      val renderer = new MuseCharRenderer(letterSpacing = letterSpacing.get,
        spaceWidth = spaceWidth.get,
        symbolFrontSpace = symbolFrontSpace.get)

      val rng = {
        RNG((seed.get * Long.MaxValue).toLong)
      }
      infoPrinter("start to render text...")
      renderer.renderTextInParallel(letterMap.get, lean = lean.get,
        maxLineWidth = maxLineWidth.get,
        breakWordThreshold = breakWordThreshold.get,
        lineSpacing = lineSpacing.get,
        randomness = randomness.get,
        lineRandomness = lineRandomness.get)(text)(rng)
    }
    infoPrinter("rendering finished.")
    infoPrinter(result.info)

    val aspectRatioOpt = {
      val as = aspectRatio.get
      if (as > 0) Some(as) else None
    }
    new PaintableResult(result, samplesPerUnit.get, pixelPerUnit.get,
      thicknessScale = thicknessScale.get,
      edgeSpace = EdgeSpace(leftEdge.get, rightEdge.get, topEdge.get, bottomEdge.get),
      wordsRestDis = wordsRestDis.get,
      screenPixelFactor = screenPixelFactor.get,
      useAspectRatio = aspectRatioOpt)
  }
}

case class DoubleFieldInfo (s: Settable[Double], fullName: String, cons: NamedConstraint[Double],
                            description: String = "missing description")


case class NamedConstraint[T](f: T => Boolean, requirementString: String)

object NamedConstraint{
  val noConstraint = NamedConstraint[Double](_ => true, "must be float number")

  val positiveConstraint = NamedConstraint[Double](_ > 0, "must be positive")
}