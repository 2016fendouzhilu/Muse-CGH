package ui.user

import java.awt.{Dimension, FlowLayout}
import javax.swing.{BoxLayout, JButton, JFrame, JPanel}

import ui.MySwing
import ui.font_editor.EditorMain
import main.ParamsCore
import utilities.{ProjectParameters, MuseCharMapLoader}

/**
 * Run Muse in GUI mode.
 */
object UIMain {
  def main(args: Array[String]) {
    val core = new ParamsCore

    val uiPanel = new UIControlPanel(core){
      core.addListener(this)
    }

    val resultFrames = new RenderResultJFrames(core)
    core.addListener(resultFrames)

    lazy val editor = new JFrame("Font Editor") {
      val p = EditorMain.makeEditorPanel()
      setContentPane(p)
      pack()
    }

    val openEditorButton = new JButton("Font Editor")
    MySwing.addAction(openEditorButton, () => {
      editor.setVisible(!editor.isVisible)
    })

    val reloadLetterMapButton = new JButton("Reload Letters")
    MySwing.addAction(reloadLetterMapButton, ()=>{
      core.letterMap.set(MuseCharMapLoader.loadDefaultCharMap())
    })

    val controlFrame = new JFrame(s"Muse (version ${ProjectParameters.versionNumber})"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(new JPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
        add(new JPanel(new FlowLayout()){
          add(openEditorButton)
          add(reloadLetterMapButton)
          add(new JPanel())
        })
        add(uiPanel)
      })
      pack()
      setVisible(true)
    }

    resultFrames.renderingFrame.setLocation(controlFrame.getWidth, 0)
    resultFrames.infoFrame.setLocation(0,controlFrame.getHeight+20)
    resultFrames.setInfoFrameSize(new Dimension(controlFrame.getWidth, 80))
  }
}
