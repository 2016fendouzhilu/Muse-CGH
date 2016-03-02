package editor

import java.awt._
import java.awt.event.{ActionEvent, ActionListener, KeyEvent, KeyAdapter}
import javax.swing._

import main.{InkCurve, Letter, PixelBoard}
import mymath.{Vec2, CubicCurve}

/**
  * Created by weijiayi on 2/29/16.
  */
object EditorMain {

  var editor = new Editor(Editing(Letter.empty, selects = Seq()))


  def main(args: Array[String]) {
    java.awt.EventQueue.invokeLater(new Runnable {
      override def run(): Unit = {
        val frame = new JFrame("Font Editor"){
          setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

          setContentPane(new JPanel(){

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
            val toolBar = new JPanel(){
              setLayout(new FlowLayout())
            }
            val mainSegButton = makeMainSegButton()
            toolBar.add(mainSegButton)

            val editingPanel = new EditingPanel(editor)

            add(toolBar)
            add(editingPanel)
          })

          pack()
          setVisible(true)
        }

      }
    })

  }

  def initLetter() = {
    val line = CubicCurve(Vec2(1,-1),Vec2(0.75,-0.75),Vec2(0.25,-0.25),Vec2(0,0))
    Letter(IndexedSeq(InkCurve(line, 10, 0.05,0.1)), IndexedSeq(), 2,2,2)
  }

  def currentLetter() = editor.currentEditing().letter

  def makeMainSegButton() = {
    val button = new JButton("Main Seg")
    button.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if(currentLetter().segs.isEmpty){
//          editor.newLetter(initLetter())
          println("new letter")
        }
      }
    })
    button
  }
}
