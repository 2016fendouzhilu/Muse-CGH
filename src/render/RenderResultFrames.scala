package render

import java.awt.Dimension
import javax.swing.{JTextArea, JScrollPane, JFrame, JPanel}

import utilities.{Settable, RNG, ChangeListener}

/**
 * Use this class to display rendering results
 */
class RenderResultFrames(core: UICore) extends ChangeListener{

  val renderingFrame = new JFrame(){
    setVisible(true)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }

  private val infoArea = new JTextArea(){
    setLineWrap(false)
    setEditable(false)
  }

  private val infoScroll = new JScrollPane(infoArea)

  val infoFrame = new JFrame("Console output"){
    setContentPane(infoScroll)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    setVisible(true)
  }

  def setInfoFrameSize(size: Dimension): Unit ={
    infoScroll.setPreferredSize(size)
    infoFrame.pack()
  }
  
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
      renderer.renderTextInParallel(core.letterMap.get, lean = core.lean.get,
        maxLineWidth = core.maxLineWidth.get,
        breakWordThreshold = core.breakWordThreshold.get,
        lineSpacing = core.lineSpacing.get,
        randomness = core.randomness.get,
        lineRandomness = core.lineRandomness.get)(text)(rng)
    }

    infoArea.setText(result.info)

    val sPane ={
      val useAspectRatio = {
        val as = core.aspectRatio.get
        if(as>0) Some(as) else None
      }
      val parameters = new RenderingResultDisplay(result, core.samplesPerUnit.get, core.pixelPerUnit.get,
        thicknessScale = core.thicknessScale.get, screenPixelFactor = 2, useAspectRatio = useAspectRatio)
      if (core.isAnimationMode) {
        val handle = new Settable[Boolean](true, ()=>Unit)
        currentAnimationHandle = Some(handle)
        parameters.showInAnimation(penSpeed = core.penSpeed.get, frameRate = core.frameRate.get, shouldRun = handle.get)
      }
      else
        parameters.showInScrollPane()
    }

    renderingFrame.setTitle(s"Result (randomness = ${core.randomness.get})")
    renderingFrame.setContentPane(sPane)
    renderingFrame.pack()
  }

}
