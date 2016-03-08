package render

import java.awt.{Dimension, FlowLayout}
import javax.swing.{JButton, BoxLayout, JPanel, JFrame}

import editor.{EditorMain, MyButton}

/**
 * Created by weijiayi on 3/7/16.
 */
object UITest {
  def main(args: Array[String]) {
    val core = new UICore

    val uiPanel = new UIControlPanel(core){
      core.addListener(this)
    }

    val resultFrame = new RenderResultPanel(core)
    core.addListener(resultFrame)

    lazy val editor = new JFrame("Font Editor") {
      val p = EditorMain.makeEditorPanel()
      setContentPane(p)
      pack()
    }

    val openEditorButton = new JButton("Font Editor")
    MyButton.addAction(openEditorButton, () => {
      editor.setVisible(!editor.isVisible)
    })

    val controlFrame = new JFrame("Control Panel"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(new JPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
        add(new JPanel(new FlowLayout()){
          add(openEditorButton)
          add(new JPanel())
        })
        add(uiPanel)
      })
      pack()
      setVisible(true)
    }

    resultFrame.setLocation(controlFrame.getWidth, 0)

  }
}
