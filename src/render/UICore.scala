package render

import utilities.ChangeSource

/**
 * Created by weijiayi on 3/6/16.
 */
class UICore() extends ChangeSource{
  val textRendered = newSettable[String]("")

  val samplesPerUnit = newSettable[Double](50.0)

  val pixelPerUnit = newSettable[Double](12.0)

  val letterSpacing = newSettable[Double](0.0)

  val spaceWidth = newSettable[Double](0.8)

  val symbolFrontSpace = newSettable[Double](0.2)

  val lean = newSettable[Double](0.3)

  val randomness = newSettable[Double](0.08)

  val seed = newSettable[Int](1)

  val maxLineWidth = newSettable[Double](50.0)

  val breakWordThreshold = newSettable[Double](6.0)

  val lineSpacing = newSettable[Double](4.0)

  val interactiveMode = newSettable[Boolean](true)

}
