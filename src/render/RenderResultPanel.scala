package render

import javax.swing.{JFrame, JPanel}

import utilities.{LetterMapLoader, ChangeListener}

/**
 * Created by weijiayi on 3/7/16.
 */
class RenderResultPanel(core: UICore) extends JFrame("Result") with ChangeListener{
  setVisible(true)

  val letterMap = LetterMapLoader.loadDefaultLetterMap()

  override def editingUpdated(): Unit = {
    val text = core.textRendered.get

    val result = {
      val renderer = new LetterRenderer(letterSpacing = core.letterSpacing.get,
        spaceWidth = core.spaceWidth.get,
        symbolFrontSpace = core.symbolFrontSpace.get)

      renderer.renderText(letterMap, lean = core.lean.get ,
        maxLineWidth = core.maxLineWidth.get,
        breakWordThreshold = core.breakWordThreshold.get,
        lineSpacing = core.lineSpacing.get)(text)
    }

    val sPane = RenderTest.showInScrollPane(result = result , dotsPerUnit = core.samplesPerUnit.get, pixelPerUnit = core.pixelPerUnit.get)
    setContentPane(sPane)
    pack()
    setVisible(true)
  }

}
