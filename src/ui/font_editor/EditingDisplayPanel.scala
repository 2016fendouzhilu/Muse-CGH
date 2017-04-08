package ui.font_editor

import java.awt._
import javax.swing.JPanel

import ui.MouseManager
import main.CurveDrawer
import utilities.{ChangeListener, CubicCurve, MyMath, Vec2}

/**
  * Panel for displaying and modifying the Editor
  */
class EditingDisplayPanel(editor: EditorCore, var pixelPerUnit: Int = 40, var displayPixelScale: Double = 4)
  extends JPanel with ChangeListener {

  var imageOffset = Vec2.zero

  val baselineColor = Color.blue.darker()
  val gridColor = Color.green
  val backgroundColor = Color.white
  val mainStrokeColor = Color.black
  val endpointColor = CurveDrawer.colorWithAlpha(Color.red, 0.75)
  val controlPointColor = CurveDrawer.colorWithAlpha(Color.orange, 0.75)
  val curveHighlightEnd = Color.orange.brighter()
  val curveHighlightStart = Color.red
  val editThicknessColor = Color.red
  val controlLineColor = new Color(0.6f,0f,0.6f)

  val letterMaxTall = 2.0
  val letterMaxDeep = 2.0
  val letterMaxWidth = 2.0

  setPreferredSize(new Dimension(windowWidthFromBoard, windowHeightFromBoard))
  setMinimumSize(new Dimension(windowWidthFromBoard, windowHeightFromBoard))
  setBackground(backgroundColor)

  def editorToDisplayPointTrans(p: Vec2): Vec2 = {
    val s = editorToDisplayLengthFactor
    Vec2(p.x*s, (p.y + letterMaxTall)*s) + imageOffset
  }

  def editorToDisplayLengthFactor = pixelPerUnit*displayPixelScale

  def displayToEditorPointTrans(p: Vec2): Vec2 = {
    val s = editorToDisplayLengthFactor
    val p1 = (p - imageOffset) / s
    Vec2(p1.x, p1.y - letterMaxTall)
  }

  def zoomCamera(scale: Double): Unit ={
    displayPixelScale *= scale
    imageOffset *= scale
    repaint()
  }

  val controlLineThickness = 3.5

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val g2d = g.asInstanceOf[Graphics2D]

    val drawer = new CurveDrawer(g2d, editorToDisplayPointTrans, pixelPerUnit*displayPixelScale)

    drawBoardLines(drawer,2,2)

    editor.currentEditing() match {
      case Editing(letter, selects) =>
        val selectedCurves = selects.map(letter.segs)
        selectedCurves.foreach(c => drawer.drawCurveControlPoints(c, curveHighlightStart, curveHighlightEnd, controlLineColor, controlLineThickness))

        editor.mode match{
          case EditControlPoint(pid) =>
            drawEditingLines(drawer)(selectedCurves.map(_.curve), pid, controlPointColor)
          case edt@EditThickness(_) =>
            drawEditingLines(drawer)(selectedCurves.map(_.curve), edt.pid, editThicknessColor)
          case BendCurve =>
            bendCurveBuffer.foreach { b =>
              b.dots.foreach { d =>
                drawer.setColor(Color.gray)
                drawer.drawDot(d, 0.02)
              }
            }
          case _ => ()
        }

        drawer.drawLetter(letter, mainStrokeColor, selects, curveHighlightStart, curveHighlightEnd )
    }

  }

  def boardHeight = MyMath.ceil((letterMaxDeep + letterMaxTall) * pixelPerUnit)

  def boardWidth = MyMath.ceil(letterMaxWidth * pixelPerUnit)

  def boardBaseLine = MyMath.ceil(letterMaxTall * pixelPerUnit)

  def windowWidthFromBoard = (boardWidth * displayPixelScale).toInt

  def windowHeightFromBoard = (boardHeight * displayPixelScale).toInt

  def displayWidth = boardWidth * displayPixelScale

  def displayHeight = boardHeight * displayPixelScale


  val baseLineThickness = 4
  val gridLineThickness = 3
  def drawBoardLines(drawer: CurveDrawer, width: Double, height: Double): Unit ={

    drawer.setColor(baselineColor)
    drawer.drawLine(Vec2.zero, Vec2(width,0), baseLineThickness, noWidthScale = true)
    drawer.drawLine(Vec2(0,-width), Vec2(0,width), baseLineThickness, noWidthScale = true)

    drawer.setColor(gridColor)
    import collection.immutable.List
    List(-2,-1,1,2).foreach(i =>{
      drawer.drawLine(Vec2(0, i), Vec2(width,i), gridLineThickness, noWidthScale = true)
    })

    drawer.drawLine(Vec2(1,-height), Vec2(1,height), gridLineThickness, noWidthScale = true)
  }

  val moveSpeed = 1.0
  var isSampling = false
  var bendCurveBuffer: Option[BendCurveBuffer] = None
  def dragAction(drag: Vec2, init: Vec2, current: Vec2) = {
    def scaleRatio(p: Vec2) = {
      val targetPoint = editorToDisplayPointTrans(p)
      val relative = current - targetPoint
      val dis = math.max((init-targetPoint).length, 0.1)
      (drag dot relative.normalized) / dis + 1
    }

    def bendCurve(): Unit = {
      editor.currentEditing().selectedInkCurves.headOption.foreach { ink =>
        def startNewBuffer() = {
          val initCurve = ink.curve
          val start = initCurve.p0
          val penOffset = displayToEditorPointTrans(current) - start
          bendCurveBuffer = Some(new BendCurveBuffer(start, penOffset, initCurve, dotsDistance = 0.03, dataPointNum = 15))
        }

        if(isSampling)
          bendCurveBuffer match {
            case Some(buffer) =>
              buffer.penMoveTo(displayToEditorPointTrans(current))
              val c1 = buffer.curve
              val c0 = ink.curve
              val (d1, d2, d3) = (c1.p1 - c0.p1, c1.p2-c0.p2, c1.p3-c0.p3)
              editor.dragControlPoint(1, d1)
              editor.dragControlPoint(3, d3)
              editor.dragControlPoint(2, d2-d3) // because drag p3 will move p2 as well
            case None =>
              startNewBuffer()
          }
        else {
          startNewBuffer()
          isSampling = true
        }
      }
    }

    editor.mode match {
      case MoveCamera =>
        dragImage(drag)
      case EditControlPoint(id) =>
        editor.dragControlPoint(id, drag/editorToDisplayLengthFactor)
      case edt@ EditThickness(isHead) =>
        editor.currentEditing().selectedInkCurves.headOption.foreach{ ink =>
          val targetPoint = ink.curve.getPoint(edt.pid)
          val ratio = scaleRatio(targetPoint)
          editor.scaleThickness(isHead, ratio)
//          drawDragHint(targetPoint, current, editThicknessColor)
        }
      case ScaleLetter =>
        val r = scaleRatio(Vec2.zero)
        editor.scaleLetter(r)
      case TranslateLetter =>
        editor.translateLetter(drag/editorToDisplayLengthFactor)
      case ScaleTotalThickness =>
        editor.scaleTotalThickness(scaleRatio(Vec2.zero))
      case BendCurve =>
        bendCurve()
    }
  }

  def dragFinishAction(): Unit = {
    editor.mode match{
      case MoveCamera => ()
      case BendCurve =>
        bendCurveBuffer.foreach(_.report())
        isSampling = false
        editor.recordNow()
        editor.notifyListeners()
      case _ => editor.recordNow()
    }
  }

  new MouseManager(this, dragAction, dragFinishAction)

  def dragImage(drag: Vec2): Unit ={
    imageOffset += drag
    repaint()
  }

  val editingLineThickness = 3
  def drawEditingLines(drawer: CurveDrawer)(curves: Seq[CubicCurve], pid: Int, color: Color): Unit ={
    drawer.setColor(color)
    curves.foreach{ c=>
      val Vec2(x,y) = c.getPoint(pid)
      drawer.drawLine(Vec2(-1,y), Vec2(3,y), editingLineThickness, noWidthScale = true, dashed = Some((8f,6f)))
      drawer.drawLine(Vec2(x, -3), Vec2(x, 3), editingLineThickness, noWidthScale = true, dashed = Some((8f,6f)))
    }
  }


  override def editingUpdated() = {
    repaint()
  }

}
