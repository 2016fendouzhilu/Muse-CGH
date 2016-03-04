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

    val initEditing = Editing(Letter(Vector(LetterSeg(CubicCurve(Vec2(0.09528437490385318,-0.83064564441715),Vec2(0.3674350986014138,-0.805673805073208),Vec2(0.7036026138374765,-0.8888427276339701),Vec2(0.8237002310212765,-0.9457700563078758)),0.02290089208453636,0.06219199182877263,false,false), LetterSeg(CubicCurve(Vec2(0.8237002310212765,-0.9457700563078758),Vec2(0.9099377270607466,-0.8875729730910561),Vec2(0.6464636594064178,-1.0674548666703159),Vec2(0.3036299328200654,-0.813503958087832)),0.06219199182877263,0.052881343253038386,true,false), LetterSeg(CubicCurve(Vec2(0.3036299328200654,-0.813503958087832),Vec2(-0.03920379376628701,-0.5595530495053485),Vec2(0.04967902423758201,-0.23153312591964037),Vec2(0.18723576638642703,-0.12148773220056454)),0.052881343253038386,0.06586609715910535,true,false), LetterSeg(CubicCurve(Vec2(0.18723576638642703,-0.12148773220056454),Vec2(0.3247925085352724,-0.011442338481488233),Vec2(0.6739750078361884,-0.043186202054298614),Vec2(0.7570381175183742,-0.19873113356106983)),0.06586609715910535,0.06724813153868209,true,false), LetterSeg(CubicCurve(Vec2(0.7570381175183742,-0.19873113356106983),Vec2(0.8401012272005609,-0.354276065067841),Vec2(0.8729032195591339,-0.49606532235972756),Vec2(0.9004145679889028,-0.908735548806263)),0.06724813153868209,0.06587320802101379,false,false), LetterSeg(CubicCurve(Vec2(0.9004145679889028,-0.908735548806263),Vec2(0.8856007649882571,-0.32676471663807133),Vec2(0.9596613149611964,-0.11505430918314154),Vec2(1.044320082852311,-0.0939763837707954)),0.06587320802101379,0.05435974275077909,true,false), LetterSeg(CubicCurve(Vec2(1.044320082852311,-0.0939763837707954),Vec2(1.128978850743425,-0.07289845835844928),Vec2(1.1924581128587601,-0.14645957154450867),Vec2(1.2559458400043781,-0.2950208530652615)),0.05435974275077909,0.010090545203537277,true,false))),List())

    val editor = new Editor(initEditing)

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
