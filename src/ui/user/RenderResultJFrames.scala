package ui.user

import java.awt.Dimension
import javax.swing.{JFrame, JScrollPane, JTextArea}

import main.ParamsCore
import utilities.{ChangeListener, Settable}

/**
 * Use this class to display rendering results
 */
class RenderResultJFrames(core: ParamsCore) extends ChangeListener{

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
    
    val resultDisplay = core.getPaintableResult(infoArea.setText)

    val sPane ={
      if (core.animationMode.get) {
        val handle = new Settable[Boolean](true, ()=>Unit)
        currentAnimationHandle = Some(handle)
        resultDisplay.showInAnimation(penSpeed = core.penSpeed.get, frameRate = core.frameRate.get, shouldRun = handle.get)
      }
      else
        resultDisplay.showInScrollPane()
    }

    renderingFrame.setTitle(s"Result (randomness = ${core.randomness.get})")
    renderingFrame.setContentPane(sPane)
    renderingFrame.pack()
  }

}
