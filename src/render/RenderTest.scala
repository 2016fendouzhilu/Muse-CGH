package render

import javax.swing.JFrame

import utilities.{RNG, LetterMapLoader}

/**
 * Created by weijiayi on 3/10/16.
 */
object RenderTest {
  def main(args: Array[String]) {
    val result = renderText()
    val dotsPerUnit = 50.0

    val pixelPerUnit = 14

    val screenFactor = 2

    val parameters = new RenderingParameters(result, dotsPerUnit, pixelPerUnit, screenPixelFactor = screenFactor)
    val p = parameters.showInAnimation(penSpeed = 40, frameRate = 60, shouldRun = true)
    //    val p = parameters.showInScrollPane()

    new JFrame("Rendering Result"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(p)
      pack()
      setVisible(true)
    }
  }

  def renderText() = {
    val renderer = new LetterRenderer(letterSpacing = 0.0, spaceWidth = 0.8, symbolFrontSpace = 0.2)
    val letterMap = LetterMapLoader.loadDefaultLetterMap()

    //    val text = "None of this had even a hope of any practical application in my life. But ten years later, when we were designing the first Macintosh computer, it all came back to me. And we designed it all into the Mac. It was the first computer with beautiful typography. If I had never dropped in on that single course in college, the Mac would have never had multiple typefaces or proportionally spaced fonts. And since Windows just copied the Mac, its likely that no personal computer would have them. If I had never dropped out, I would have never dropped in on this calligraphy class, and personal computers might not have the wonderful typography that they do. Of course it was impossible to connect the dots looking forward when I was in college. But it was very, very clear looking backwards ten years later."
    val text = "Thousands cities from home, wander into the unknown. Chances are here I was told, Crossing the footsteps of new and of old"

    renderer.renderTextInParallel(letterMap, lean = 0.3, maxLineWidth = 30, breakWordThreshold = 5,
      lineSpacing = 4, randomness = 0.04)(text)(RNG(1))._1
  }
}
