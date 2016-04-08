package render

import utilities.ChangeSource

/**
 * Created by weijiayi on 3/6/16.
 */
class UICore() extends ChangeSource {
  val textRendered = newSettable[String]("")

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

  val lineSpacing = newSettable[Double](4.0)

  val interactiveMode = newSettable[Boolean](true)

  val penSpeed = newSettable[Double](40)

  val frameRate = newSettable[Double](60)

  var isAnimationMode = false

  var animationFinished = true

}
