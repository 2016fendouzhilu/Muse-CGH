package render

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics, Graphics2D, RenderingHints}
import javax.swing._

import editor.MyButton
import utilities.{RNG, LetterMapLoader, Vec2}

/**
  * Created by weijiayi on 3/4/16.
  */
class RenderingParameters(result: RenderingResult, dotsPerUnit: Double,
                          pixelPerUnit: Double, screenPixelFactor: Int,
                          wordsRestDis: Double = 5, thicknessScale: Double,
                          backgroundColor: Color = Color.white, penColor: Color = Color.black){
  val edge = (pixelPerUnit * 2).toInt
  val topHeight = (3*pixelPerUnit).toInt
  val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit , result.height * pixelPerUnit)
  val screenSize = new Dimension(imgWidth.toInt + 2 * edge, imgHeight.toInt + 2 * edge + topHeight)
  val totalSize = new Dimension(screenSize.width * screenPixelFactor, screenSize.height * screenPixelFactor)
  val imageOffset = Vec2(edge, edge + topHeight)

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
          val painter = new LetterPainter(bufferG, pixelPerUnit = pixelPerUnit, displayPixelScale = 1,
            imageOffset = imageOffset, dotsPerUnit = dotsPerUnit, thicknessScale = thicknessScale)

          val stop = painter.drawAnimation(screenPixelFactor, shouldStop)(mainSegs ++ secondSegs,
            offset, penColor) || shouldStop(wordsRestDis)
          if(stop) return true
      }
      false
    }

    val startButton = new JButton("Start")
    MyButton.addAction(startButton, () => {
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

  def showInScrollPane(): JScrollPane = {
    new JScrollPane(new JPanel() {
      setBackground(backgroundColor)
      setPreferredSize(screenSize)

      def drawToGraphics(g: Graphics): Unit = {
        val g2d = g.asInstanceOf[Graphics2D]

        result.words.foreach {
          case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
            val painter = new LetterPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = screenPixelFactor,
              imageOffset = imageOffset, dotsPerUnit = dotsPerUnit, thicknessScale = thicknessScale)

            painter.draw(mainSegs ++ secondSegs, offset, penColor)
        }
      }

      drawToGraphics(buffer.getGraphics)

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)

        g.drawImage(buffer, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    }){
      setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
    }
  }

}

