package editor

import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.{BoxLayout, JComponent, JFrame, JPanel}

import main.{InkCurve, Letter}
import utilities.{CubicCurve, Vec2}

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
    ), 2,2,2)
  }


  def addPointToCurve(curve: CubicCurve, control: Vec2, pass: Vec2): CubicCurve = {
    CubicCurve(curve.p3, curve.p3*2-curve.p2, control, pass)
  }

  def main(args: Array[String]) {
    val editing = Editing(Letter(Vector(InkCurve(CubicCurve(Vec2(0.1250000000000002,-0.7083333333333336),Vec2(0.34999999999999987,-0.8166666666666674),Vec2(0.6416666666666658,-0.8250000000000006),Vec2(0.7095833333333333,-0.7791666666666668)),50,0.15666611874469452,0.12376443319852101,false,false), InkCurve(CubicCurve(Vec2(0.7095833333333333,-0.7791666666666668),Vec2(0.7775000000000009,-0.733333333333333),Vec2(0.57,-0.8750000000000001),Vec2(0.3000000000000001,-0.675)),50,0.12376443319852101,0.1326788186823617,true,false), InkCurve(CubicCurve(Vec2(0.3000000000000001,-0.675),Vec2(0.03000000000000025,-0.475),Vec2(0.09166666666666667,-0.16666666666666669),Vec2(0.2,-0.08)),50,0.1326788186823617,0.13732009300943046,true,false), InkCurve(CubicCurve(Vec2(0.2,-0.08),Vec2(0.30833333333333335,0.006666666666666678),Vec2(0.5916666666666669,-0.06833333333333325),Vec2(0.6570833333333329,-0.19083333333333333)),50,0.13732009300943046,0.10500000000000001,true,false), InkCurve(CubicCurve(Vec2(0.6570833333333329,-0.19083333333333333),Vec2(0.7224999999999989,-0.3133333333333334),Vec2(0.7483333333333333,-0.4250000000000002),Vec2(0.77,-0.75)),50,0.10500000000000001,0.16687633358805154,false,false), InkCurve(CubicCurve(Vec2(0.77,-0.75),Vec2(0.7583333333333335,-0.2916666666666662),Vec2(0.8166600000000002,-0.12493333333333331),Vec2(0.8833333333333335,-0.10833333333333334)),50,0.16687633358805154,0.14,true,false), InkCurve(CubicCurve(Vec2(0.8833333333333335,-0.10833333333333334),Vec2(0.9500066666666669,-0.09173333333333336),Vec2(1.0000000000000004,-0.14966666666666667),Vec2(1.0500000000000005,-0.26666666666666655)),50,0.14,0.01,true,false)),2.0,2.0,2.0),List(0))


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
