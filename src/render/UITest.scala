package render

import javax.swing.JFrame

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

    val controlFrame = new JFrame("Control Panel"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(uiPanel)
      pack()
      setVisible(true)
    }

    resultFrame.setLocation(controlFrame.getWidth, 0)

  }
}
