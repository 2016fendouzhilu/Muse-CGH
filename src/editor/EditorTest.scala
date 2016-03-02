package editor

import java.awt.FlowLayout
import java.awt.event.{MouseEvent, MouseAdapter, KeyEvent, KeyAdapter}
import javax.swing.{JComponent, BoxLayout, JPanel, JFrame}

import main.{InkCurve, Letter}
import mymath.{Vec2, CubicCurve}

/**
  * Created by weijiayi on 2/29/16.
  */
object EditorTest {
  def initLetter() = {
    val line0 = CubicCurve(Vec2(0.8,-0.8),Vec2(0.6,-0.9),Vec2(0.52,-0.9),Vec2(0.25,-0.7))
    val line1 = addPointToCurve(line0, Vec2(0.1,-0.25), Vec2(0.2,-0.08))
    val line2 = addPointToCurve(line1, Vec2(0.64,-0.1), Vec2(0.77, -0.75))
    val line3 = CubicCurve(Vec2(0.77, -0.75),Vec2(0.64,-0.1), Vec2(0.8, -0.1), Vec2(0.9,-0.1))
    val line4 = addPointToCurve(line3, Vec2(0.8,-0), Vec2(1.0, -0.3))
    Letter(IndexedSeq(
      InkCurve(line0, 50, 0.11, 0.09),
      InkCurve(line1, 50, 0.09, 0.08),
      InkCurve(line2, 50, 0.08, 0.13),
      InkCurve(line3, 50, 0.13, 0.14),
      InkCurve(line4, 50, 0.14, 0.01)
    ), IndexedSeq(), 2,2,2)
  }


  def addPointToCurve(curve: CubicCurve, control: Vec2, pass: Vec2): CubicCurve = {
    CubicCurve(curve.p3, curve.p3*2-curve.p2, control, pass)
  }

  def main(args: Array[String]) {
    val editing = Editing(Letter(Vector(
      InkCurve(CubicCurve(Vec2(0.8,-0.8),Vec2(0.6,-0.9),Vec2(0.52,-0.9),Vec2(0.25,-0.7)),50,0.11,0.09),
      InkCurve(CubicCurve(Vec2(0.25,-0.7),Vec2(-0.02,-0.5),Vec2(0.1,-0.25),Vec2(0.2,-0.08)),50,0.09,0.08),
      InkCurve(CubicCurve(Vec2(0.2,-0.08),Vec2(0.3,0.09),Vec2(0.64,-0.1),Vec2(0.77,-0.75)),50,0.08,0.13),
      InkCurve(CubicCurve(Vec2(0.77,-0.75),Vec2(0.64,-0.1),Vec2(0.84166,-0.0416),Vec2(0.9,-0.1)),50,0.13,0.14),
      InkCurve(CubicCurve(Vec2(0.9,-0.1),Vec2(0.958,-0.1583),Vec2(0.95,-0.183),Vec2(1.0,-0.3)),50,0.14,0.01)
    ),Vector(),2.0,2.0,2.0),List(0))

    val editor = new Editor(editing)

    val editingPanel = new EditingPanel(editor, displayPixelScale = 3){
      editor.addListener(this)
    }

    val controlPanel = new ControlPanel(editor){
      editor.addListener(this)
    }

    new JFrame("Font Editor") {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(new JPanel(){
        setFocusable(true)
        addKeyListener(controlPanel.makeKeyListener())
        def gainFocus() = this.requestFocusInWindow()

        def setFocus(jComponent: JComponent): Unit ={
          jComponent.addMouseListener(new MouseAdapter {
            override def mouseClicked(e: MouseEvent): Unit = gainFocus()
          })
        }

        setFocus(editingPanel)
        setFocus(controlPanel)

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
        add(editingPanel)
        add(controlPanel)


      })

      pack()
      setVisible(true)
    }
  }

}
