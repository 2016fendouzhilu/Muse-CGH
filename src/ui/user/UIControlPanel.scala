package ui.user

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Dimension, FlowLayout}
import javax.swing._
import javax.swing.text.JTextComponent

import ui.MySwing
import main.{DoubleFieldInfo, ParamsCore}
import utilities.{ChangeListener, Settable, ValueTextComponent}

import scala.collection.mutable.ListBuffer

/**
 * Control the parameters of ParamsCore for rendering
 */
class UIControlPanel(core: ParamsCore) extends JPanel with ChangeListener {

  def textToDouble(s: String) = {
    try{ Some(s.toDouble) }
    catch { case e: Throwable => None}
  }

  private val updateList = new ListBuffer[ValueTextComponent[_,_]]()

  private def makeDoubleFiled(settable: Settable[Double], constraint: Double => Boolean = _ => true, toolTip: String):
      ValueTextComponent[Double, JTextField] = {
    val field = new JTextField() {setToolTipText(toolTip)}

    val vtc = new ValueTextComponent[Double, JTextField](s => textToDouble(s).filter(constraint), _.toString,
      getValue = () => settable.get,
      setValue = settable.set,
      component = field,
      confirmKeys = Set(KeyEvent.VK_ENTER),
      cancelKeys = Set(KeyEvent.VK_ESCAPE)
    )

    updateList.append(vtc)
    vtc
  }


  val textArea = new JTextArea {
    setLineWrap(true)

    addKeyListener(new KeyAdapter {
      override def keyReleased(e: KeyEvent): Unit = {
        def isArrowKey(code: Int) = {
          List(KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_DOWN).contains(code)
        }

        if(core.interactiveMode.get && !isArrowKey(e.getKeyCode)){
          core.textToRender.set(getText)
        }
      }
    })
  }

  val renderButton = new JButton("Render Text")
  MySwing.addAction(renderButton, () => core.textToRender.set(textArea.getText))
  
  val interactiveCheckBox = new JCheckBox("Interactive")
  MySwing.addAction(interactiveCheckBox, ()=> core.interactiveMode.set(interactiveCheckBox.isSelected))

  val animationCheckBox = new JCheckBox("Animation")
  MySwing.addAction(animationCheckBox, () => {
    core.animationMode.set(animationCheckBox.isSelected)
    core.textToRender.set(textArea.getText)
  })

  private def makeLabeledDoubleField(info: DoubleFieldInfo) = info match {
    case DoubleFieldInfo(settable, label, cons, desc) =>
      (new JLabel(label){setToolTipText(desc)},
        makeDoubleFiled(settable, cons.f, desc))
  }

  setupLayout()

  def setupLayout(): Unit = {
    val parametersPanel = new JPanel{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

      def addARow[C <: JTextComponent](pairs: Seq[(JLabel,ValueTextComponent[_,C])]) = {
        this.add(new JPanel(new FlowLayout()){
          pairs.foreach{
            case (l,f) =>
              add(l)
              add(f.component)
          }
        })
      }

      List(core.fontRow, core.layoutRow, core.edgeRow, core.wordRow, core.randomRow, core.animationRow).foreach(row =>
        addARow(row.map(makeLabeledDoubleField)))

    }

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    add(parametersPanel)
    add(new JScrollPane(textArea){setPreferredSize(new Dimension(500,350))})
    add(new JPanel(new FlowLayout()){
      add(interactiveCheckBox)
      add(renderButton)
      add(animationCheckBox)
    })
  }


  override def editingUpdated(): Unit = {
    updateList.foreach(_.updateText())
    interactiveCheckBox.setSelected(core.interactiveMode.get)
  }

}


