package render

import java.awt.{Dimension, FlowLayout}
import java.awt.event.{KeyEvent, KeyAdapter, FocusEvent, FocusAdapter}
import javax.swing._
import javax.swing.text.JTextComponent

import editor.MyButton
import render.UIPanel.DoubleFieldInfo
import utilities.{ValueTextComponent, Settable, ChangeListener}

import scala.collection.mutable.ListBuffer

/**
 * Control the parameters of UICore for rendering
 */
class UIPanel(core: UICore) extends JPanel with ChangeListener {

  def textToDouble(s: String) = {
    try{ Some(s.toDouble) }
    catch { case e: Throwable => None}
  }

  val updateList = new ListBuffer[ValueTextComponent[_,_]]()

  def makeDoubleFiled(settable: Settable[Double], constraint: Double => Boolean = _ => true):
      ValueTextComponent[Double, JTextField] = {
    val field = new JTextField()

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

  val noConstraint = (_: Double) => true

  val positiveConstraint = (x: Double) => x > 0

  val fontRow = List[DoubleFieldInfo] (
    (core.pixelPerUnit, "Font size", positiveConstraint),
    (core.samplesPerUnit, "Samples", positiveConstraint),
    (core.lean, "Lean", noConstraint)
  ).map(makeLabeledDoubleField)

  val layoutRow = List[DoubleFieldInfo] (
    (core.maxLineWidth, "Line width", (w: Double) => w > 0 && w > core.breakWordThreshold.get),
    (core.lineSpacing, "Line spacing", positiveConstraint),
    (core.breakWordThreshold, "Break threshold", (b: Double) => b > 0 && b < core.maxLineWidth.get)
  ).map(makeLabeledDoubleField)

  val wordRow = List[DoubleFieldInfo] (
    (core.spaceWidth, "Space width", positiveConstraint),
    (core.letterSpacing, "Letter spacing", noConstraint),
    (core.symbolFrontSpace, "Mark spacing", noConstraint)
  ).map(makeLabeledDoubleField)

  val textArea = new JTextArea { setPreferredSize(new Dimension(500,400)) }

  val renderButton = new JButton("Render Text")
  MyButton.addAction(renderButton, () => core.textRendered.set(textArea.getText))

  def makeLabeledDoubleField(info: DoubleFieldInfo) = info match {
    case (settable, label, cons) =>
      (new JLabel(label), makeDoubleFiled(settable, cons))
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

      addARow(fontRow)
      addARow(layoutRow)
      addARow(wordRow)
    }

    val area = new JScrollPane(textArea)

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    add(parametersPanel)
    add(area)
    add(renderButton)
  }


  override def editingUpdated(): Unit = {
    updateList.foreach(_.updateText())
  }

}

object UIPanel {
  type DoubleFieldInfo = (Settable[Double], String, Double => Boolean)
}
