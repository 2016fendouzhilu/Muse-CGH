package render

import javax.swing.{JFrame, JPanel}

import utilities.{Settable, RNG, LetterMapLoader, ChangeListener}

/**
 * Use this class to display rendering results
 */
class RenderResultPanel(core: UICore) extends JFrame with ChangeListener{
  setVisible(true)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  val letterMap = LetterMapLoader.loadDefaultLetterMap()
  
  var currentAnimationHandle: Option[Settable[Boolean]] = None

  override def editingUpdated(): Unit = {
    currentAnimationHandle.foreach(s => s.set(false))
    
    val text = core.textRendered.get

    val (result, _) = {
      val renderer = new LetterRenderer(letterSpacing = core.letterSpacing.get,
        spaceWidth = core.spaceWidth.get,
        symbolFrontSpace = core.symbolFrontSpace.get)

      val rng = {
        RNG((core.seed.get*Long.MaxValue).toLong)
      }
      renderer.renderTextInParallel(letterMap, lean = core.lean.get,
        maxLineWidth = core.maxLineWidth.get,
        breakWordThreshold = core.breakWordThreshold.get,
        lineSpacing = core.lineSpacing.get,
        randomness = core.randomness.get,
        lineRandomness = core.lineRandomness.get)(text)(rng)
    }

    val sPane ={
      val parameters = new RenderingParameters(result, core.samplesPerUnit.get, core.pixelPerUnit.get,
        thicknessScale = core.thicknessScale.get, screenPixelFactor = 2)
      if (core.isAnimationMode) {
        val handle = new Settable[Boolean](true, ()=>Unit)
        currentAnimationHandle = Some(handle)
        parameters.showInAnimation(penSpeed = core.penSpeed.get, frameRate = core.frameRate.get, shouldRun = handle.get)
      }
      else
        parameters.showInScrollPane()
    }

    setTitle(s"Result (randomness = ${core.randomness.get})")
    setContentPane(sPane)
    pack()
  }

}
