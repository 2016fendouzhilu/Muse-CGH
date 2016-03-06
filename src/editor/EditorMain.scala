package editor

import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.{BoxLayout, JComponent, JFrame, JPanel}

import utilities.{CubicCurve, Vec2}

/**
  * The entrance of editor GUI.
  */
object EditorMain {

  def addPointToCurve(curve: CubicCurve, control: Vec2, pass: Vec2): CubicCurve = {
    CubicCurve(curve.p3, curve.p3*2-curve.p2, control, pass)
  }

  def main(args: Array[String]) {

//    val initEditing = Editing(Letter(Vector(LetterSeg(CubicCurve(Vec2(0.05000000000000002,-0.6499999999999999),Vec2(0.20033333333333286,-0.6986666666666663),Vec2(0.25833333333333297,-0.7750000000000006),Vec2(0.3666666666666666,-0.9500000000000001)),0.0272121915776861,0.08753306005474445,false,false), LetterSeg(CubicCurve(Vec2(0.3666666666666666,-0.9500000000000001),Vec2(0.24166666666666642,-0.6333333333333334),Vec2(0.25133333333333296,-0.13100000000000042),Vec2(0.33833333333333304,-0.07500000000000001)),0.08753306005474445,0.06707371850323839,true,false), LetterSeg(CubicCurve(Vec2(0.33833333333333304,-0.07500000000000001),Vec2(0.42533333333333373,-0.018999999999999607),Vec2(0.5070000000000009,-0.14233333333333387),Vec2(0.5623333333333341,-0.30066666666666647)),0.06707371850323839,0.026560276887860845,false,true), LetterSeg(CubicCurve(Vec2(0.42833333333333384,-1.2396666666666651),Vec2(0.47000000000000075,-1.197999999999998),Vec2(0.45600000000000074,-1.2076666666666664),Vec2(0.5030000000000009,-1.1709999999999994)),0.08471042728269712,0.03721344838982525,true,true))),List(3))

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
