package main

import gui.user.RenderingResultDisplay
import utilities.{ChangeSource, MapLoader, RNG, Settable}

/**
 * Stores all the user-settable parameters
 */
class ParamsCore() extends ChangeSource {
  val textToRender = newSettable[String]("")

  val samplesPerUnit = newSettable[Double](50.0)

  val pixelPerUnit = newSettable[Double](10.0)

  val letterSpacing = newSettable[Double](0.0)

  val spaceWidth = newSettable[Double](0.8)

  val symbolFrontSpace = newSettable[Double](0.4)

  val lean = newSettable[Double](0.3)

  val thicknessScale = newSettable[Double](2.8)

  val randomness = newSettable[Double](0.07)

  val lineRandomness = newSettable[Double](0.1)

  val seed = newSettable[Double](0.0)

  val maxLineWidth = newSettable[Double](90.0)

  val breakWordThreshold = newSettable[Double](4.0)

  /**
   * use aspect ratio only if this value is positive */
  val aspectRatio = newSettable[Double](-1.0)

  val lineSpacing = newSettable[Double](4.0)

  val interactiveMode = newSettable[Boolean](true)

  val penSpeed = newSettable[Double](40)

  val frameRate = newSettable[Double](60)

  val letterMap = newSettable(MapLoader.loadDefaultCharMap())

  /**
   * Set to 2 for Retina display, 1 for normal
   */
  val screenPixelFactor = newSettable[Int](2)

  var isAnimationMode = false

  var animationFinished = true

  val noConstraint = NamedConstraint[Double](_ => true, "must be float number")

  val positiveConstraint = NamedConstraint[Double](_ > 0, "must be positive")

  val fontRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(pixelPerUnit, "Pixel density", positiveConstraint,
      description =  "how many pixels per unit length, a lower case 'a' is about 1 unit high"),
    DoubleFieldInfo(samplesPerUnit, "Samples", positiveConstraint,
      description = "how many quadrilaterals are used per unit length"),
    DoubleFieldInfo(lean, "Lean", noConstraint, description = "tan value of slopping angle"),
    DoubleFieldInfo(thicknessScale, "Thickness", positiveConstraint, description = "the thickness of strokes")
  )

  val randomRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(randomness, "Letter Random", noConstraint, description = "how much randomness is used for letters in words"),
    DoubleFieldInfo(lineRandomness, "Line Random", noConstraint,
      description = "how much randomness is used for height perturbation of words in a line"),
    DoubleFieldInfo(seed, "Seed", NamedConstraint[Double](s => s >= -1 && s <= 1.0, "must be within -1.0 and 1.0"),
      description = "seed value used to generate random numbers")
  )

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

  val wordRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(spaceWidth, "Space width", positiveConstraint,
      description = "the size in unit for a whitespace"),
    DoubleFieldInfo(letterSpacing, "Letter spacing", noConstraint,
      description = "extra spacing between letters in a word"),
    DoubleFieldInfo(symbolFrontSpace, "Mark spacing", noConstraint,
      description = "extra spacing for punctuation makrs and numbers")
  )

  val animationRow = List[DoubleFieldInfo] (
    DoubleFieldInfo(penSpeed, "Animation Speed", positiveConstraint,
      description = "how fast the pen moves during animation"),
    DoubleFieldInfo(frameRate, "Animation FPS", positiveConstraint,
      description = "the updating frequency in animation")
  )


  def renderingResultDisplay(infoPrinter: String=>Unit): RenderingResultDisplay = {

    val text = textToRender.get

    val (result, _) = {
      val renderer = new MuseCharRenderer(letterSpacing = letterSpacing.get,
        spaceWidth = spaceWidth.get,
        symbolFrontSpace = symbolFrontSpace.get)

      val rng = {
        RNG((seed.get * Long.MaxValue).toLong)
      }
      renderer.renderTextInParallel(letterMap.get, lean = lean.get,
        maxLineWidth = maxLineWidth.get,
        breakWordThreshold = breakWordThreshold.get,
        lineSpacing = lineSpacing.get,
        randomness = randomness.get,
        lineRandomness = lineRandomness.get)(text)(rng)
    }

    infoPrinter(result.info)

    val aspectRatioOpt = {
      val as = aspectRatio.get
      if (as > 0) Some(as) else None
    }
    new RenderingResultDisplay(result, samplesPerUnit.get, pixelPerUnit.get,
      thicknessScale = thicknessScale.get, screenPixelFactor = screenPixelFactor.get, useAspectRatio = aspectRatioOpt)
  }
}

case class DoubleFieldInfo (s: Settable[Double], fullName: String, cons: NamedConstraint[Double],
                            description: String = "missing description")


case class NamedConstraint[T](f: T => Boolean, requirementString: String)