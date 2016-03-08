package render

import javax.swing.{JFrame, JPanel}

import utilities.{RNG, LetterMapLoader, ChangeListener}

/**
 * Created by weijiayi on 3/7/16.
 */
class RenderResultPanel(core: UICore) extends JFrame with ChangeListener{
  setVisible(true)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  val letterMap = LetterMapLoader.loadDefaultLetterMap()

  override def editingUpdated(): Unit = {
    val text = core.textRendered.get

    val (result, _) = {
      val renderer = new LetterRenderer(letterSpacing = core.letterSpacing.get,
        spaceWidth = core.spaceWidth.get,
        symbolFrontSpace = core.symbolFrontSpace.get)

      renderer.renderTextInParallel(letterMap, lean = core.lean.get ,
        maxLineWidth = core.maxLineWidth.get,
        breakWordThreshold = core.breakWordThreshold.get,
        lineSpacing = core.lineSpacing.get,
        randomness = core.randomness.get)(text)(RNG(core.seed.get))
    }

    val sPane = RenderTest.showInScrollPane(result = result , dotsPerUnit = core.samplesPerUnit.get,
      pixelPerUnit = core.pixelPerUnit.get, screenPixelFactor = 2)
    setTitle(s"Result (randomness = ${core.randomness.get})")
    setContentPane(sPane)
    pack()
  }

}
