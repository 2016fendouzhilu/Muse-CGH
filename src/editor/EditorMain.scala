package editor

import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.{BoxLayout, JComponent, JFrame, JPanel}

import main.{Letter, LetterSeg}
import utilities.{CubicCurve, Vec2}

/**
  * The entrance of editor GUI.
  */
object EditorMain {

  def addPointToCurve(curve: CubicCurve, control: Vec2, pass: Vec2): CubicCurve = {
    CubicCurve(curve.p3, curve.p3*2-curve.p2, control, pass)
  }

  def main(args: Array[String]) {

    val initEditing = Editing(Letter(Vector(LetterSeg(CubicCurve(Vec2(0.11458333333333354,-0.6458333333333338),Vec2(0.33958333333333324,-0.7541666666666677),Vec2(0.6416666666666658,-0.8250000000000006),Vec2(0.7095833333333333,-0.7791666666666668)),0.04315272728080325,0.061633926189608716,false,false), LetterSeg(CubicCurve(Vec2(0.7095833333333333,-0.7791666666666668),Vec2(0.7775000000000009,-0.733333333333333),Vec2(0.57,-0.8750000000000001),Vec2(0.3000000000000001,-0.675)),0.061633926189608716,0.05240682459308606,true,false), LetterSeg(CubicCurve(Vec2(0.3000000000000001,-0.675),Vec2(0.03000000000000025,-0.475),Vec2(0.1,-0.21666666666666667),Vec2(0.20833333333333334,-0.13)),0.05240682459308606,0.06527506277462172,true,false), LetterSeg(CubicCurve(Vec2(0.20833333333333334,-0.13),Vec2(0.31666666666666665,-0.043333333333333335),Vec2(0.5916666666666669,-0.06833333333333325),Vec2(0.6570833333333329,-0.19083333333333333)),0.06527506277462172,0.0535677596049693,true,false), LetterSeg(CubicCurve(Vec2(0.6570833333333329,-0.19083333333333333),Vec2(0.7224999999999989,-0.3133333333333334),Vec2(0.7483333333333333,-0.4250000000000002),Vec2(0.77,-0.75)),0.0535677596049693,0.06528210982883437,false,false), LetterSeg(CubicCurve(Vec2(0.77,-0.75),Vec2(0.7583333333333335,-0.2916666666666662),Vec2(0.8166600000000002,-0.12493333333333331),Vec2(0.8833333333333335,-0.10833333333333334)),0.06528210982883437,0.05387195801047797,true,false), LetterSeg(CubicCurve(Vec2(0.8833333333333335,-0.10833333333333334),Vec2(0.9500066666666669,-0.09173333333333336),Vec2(1.0000000000000004,-0.14966666666666667),Vec2(1.0500000000000005,-0.26666666666666655)),0.05387195801047797,0.01,true,false)),2.0,2.0,2.0),List(1))

    val editor = new Editor(Editing.empty)

    val editingPanel = new EditingPanel(editor, displayPixelScale = 3){
      editor.addListener(this)
    }

    val controlPanel = new ControlPanel(editor, zoomAction = editingPanel.zoomCamera){
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
