package ui.user

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics, Graphics2D}
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import ui.MySwing
import main.{MuseCharPainter, RenderingResult, RenderingWord}
import utilities.{ParallelOp, ImageSaver, Vec2}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool

case class EdgeSpace(left: Double, right: Double, top: Double, bottom: Double)

/**
  * paint RenderingResult to an image, a scroll panel or make some animation
  */
class PaintableResult(result: RenderingResult, dotsPerUnit: Double,
                          pixelPerUnit: Double, screenPixelFactor: Int, thicknessScale: Double,
                          edgeSpace: EdgeSpace,
                          wordsRestDis: Double = 5,
                          backgroundColor: Color = Color.white, penColor: Color = Color.black,
                          useAspectRatio: Option[Double] = None
                       ){
  val List(leftEdge, rightEdge, topEdge, bottomEdge) =
    List(edgeSpace.left, edgeSpace.right, edgeSpace.top, edgeSpace.bottom).map{x =>
      (x * pixelPerUnit).toInt
    }

  val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit , result.height * pixelPerUnit)

  val screenWidth = imgWidth.toInt + leftEdge + rightEdge
  val screenHeight = useAspectRatio match{
    case Some(r) => (screenWidth * r).toInt
    case None => imgHeight.toInt + topEdge + bottomEdge
  }
  val screenSize = new Dimension(screenWidth, screenHeight)
  val totalSize = new Dimension(screenSize.width * screenPixelFactor, screenSize.height * screenPixelFactor)
  val imageOffset = Vec2(leftEdge, topEdge)

  val buffer = new BufferedImage(totalSize.width, totalSize.height, BufferedImage.TYPE_INT_ARGB)

  def clearImageBuffer(): Unit = {
    val g = buffer.getGraphics
    g.setColor(backgroundColor)
    g.fillRect(0,0, buffer.getWidth, buffer.getHeight)
  }

  def showInAnimation(penSpeed: => Double, frameRate: => Double, shouldRun: => Boolean): JPanel = {

    val canvas = new JPanel(){
      setBackground(backgroundColor)
      setPreferredSize(screenSize)

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        g.drawImage(buffer, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    }

    def drawAndSleep(penStartFrom: Vec2): Boolean = {
      val bufferG = buffer.getGraphics.asInstanceOf[Graphics2D]

      var timer = 0.0

      def shouldStop(dis: Double): Boolean = {
        if(!shouldRun) return true
        val timePerFrame = 1.0/frameRate
        timer += dis/penSpeed
        if(timer > timePerFrame){
          val millis = (timePerFrame / 0.001).toInt
          val nanos = ((timePerFrame % 0.001) * 1000000).toInt
          timer -= timePerFrame
          canvas.repaint()
          Thread.sleep(millis, nanos)
        }
        false
      }

      result.words.foreach {
        case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
          val painter = new MuseCharPainter(bufferG, pixelPerUnit = pixelPerUnit, displayPixelScale = 1,
            imageOffset = imageOffset, dotsPerUnit = dotsPerUnit, thicknessScale = thicknessScale)

          val stop = painter.drawAnimation(screenPixelFactor, shouldStop)(mainSegs ++ secondSegs,
            offset, penColor) || shouldStop(wordsRestDis)
          if(stop) return true
      }
      false
    }

    val startButton = new JButton("Start")
    MySwing.addAction(startButton, () => {
      startButton.setEnabled(false)
      new Thread(new Runnable {
        override def run(): Unit = {
          clearImageBuffer()
          canvas.repaint()
          drawAndSleep(Vec2.zero)
          startButton.setEnabled(true)
        }
      }).start()

    })

    new JPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(startButton)
      add(new JScrollPane(canvas))
    }
  }
  
  def drawToBuffer(): Unit ={
    val start = System.currentTimeMillis()
    val g2d = buffer.getGraphics.asInstanceOf[Graphics2D]
    val painter = new MuseCharPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = screenPixelFactor,
      imageOffset = imageOffset, dotsPerUnit = dotsPerUnit, thicknessScale = thicknessScale)

    result.words.foreach {
      case (offset, RenderingWord(mainSegs, secondSegs, width)) =>
        painter.draw(mainSegs ++ secondSegs, offset, penColor)
    }
    val timeUse = System.currentTimeMillis() - start
    println(s"painting time use $timeUse ms.")
  }

  def drawToBufferInParallel(threadNum: Int = 10): Unit ={
    val start = System.currentTimeMillis()
    val g2d = buffer.getGraphics.asInstanceOf[Graphics2D]

    val painter = new MuseCharPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = screenPixelFactor,
      imageOffset = imageOffset, dotsPerUnit = dotsPerUnit, thicknessScale = thicknessScale)

    val wordsPar = result.words.par
    wordsPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    wordsPar.foreach {
      case (offset, RenderingWord(mainSegs, secondSegs, width)) =>
        painter.paintWordWithBuffering(mainSegs ++ secondSegs, offset, penColor, width, height = 4)
    }

    val timeUse = System.currentTimeMillis() - start
    println(s"painting time use $timeUse ms. ($threadNum threads)")
  }

  def showInScrollPane(): JPanel = {
    val scrollPane = new JScrollPane(new JPanel() {
      setBackground(backgroundColor)
      setPreferredSize(screenSize)
      
      drawToBuffer()

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)

        g.drawImage(buffer, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    }){
      setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
    }

    val saveButton = new JButton("Save Image")
    MySwing.addAction(saveButton, openSaveImageDialog)

    new JPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(saveButton)
      add(scrollPane)
    }
  }

  def openSaveImageDialog(): Unit ={
    val defaultPath = Paths.get("").toAbsolutePath.toFile
    val fc = new JFileChooser(defaultPath){
      setFileFilter(new FileNameExtensionFilter("PNG Files", "png"))
      setMultiSelectionEnabled(false)
    }
    fc.showSaveDialog(null) match {
      case JFileChooser.APPROVE_OPTION =>
        val path = fc.getSelectedFile.getAbsolutePath
        ImageSaver.saveImage(buffer, path)
    }
  }

}

