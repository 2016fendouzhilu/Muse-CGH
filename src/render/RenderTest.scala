package render

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics, Graphics2D, RenderingHints}
import javax.swing._

import editor.MyButton
import utilities.{LetterMapLoader, Vec2}

/**
  * Created by weijiayi on 3/4/16.
  */
object RenderTest {

  def main(args: Array[String]) {
    val result = renderText()
    val dotsPerUnit = 50.0

    val pixelPerUnit = 14

    val screenFactor = 2

    val p = showInAnimation(result, dotsPerUnit, pixelPerUnit, penSpeed = 40, frameRate = 60, screenPixelFactor = screenFactor)
//    val p = showInScrollPane(result, dotsPerUnit, pixelPerUnit, screenPixelFactor = screenFactor)

    new JFrame("Rendering Result"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(p)
      pack()
      setVisible(true)
    }
  }

  def renderText() = {
    val renderer = new LetterRenderer(letterSpacing = 0.0, spaceWidth = 0.8, symbolFrontSpace = 0.2)
    val letterMap = LetterMapLoader.loadDefaultLetterMap()

//    val text = "None of this had even a hope of any practical application in my life. But ten years later, when we were designing the first Macintosh computer, it all came back to me. And we designed it all into the Mac. It was the first computer with beautiful typography. If I had never dropped in on that single course in college, the Mac would have never had multiple typefaces or proportionally spaced fonts. And since Windows just copied the Mac, its likely that no personal computer would have them. If I had never dropped out, I would have never dropped in on this calligraphy class, and personal computers might not have the wonderful typography that they do. Of course it was impossible to connect the dots looking forward when I was in college. But it was very, very clear looking backwards ten years later."
//    val text = "Thousands cities from home, wander into the unknown. Chances are here I was told, Crossing the footsteps of new and of old"
    val text = "Designing of beautiful fonts."

    renderer.renderTextInParallel(letterMap, lean = 0.3, maxLineWidth = 30, breakWordThreshold = 5, lineSpacing = 4)(text)
  }

  def showInAnimation(result: RenderingResult, dotsPerUnit: Double,
                      pixelPerUnit: Double, penSpeed: Double, frameRate: Double, screenPixelFactor: Int): JPanel = {
    val edge = (pixelPerUnit * 2).toInt
    val topHeight = (2*pixelPerUnit).toInt
    val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit , result.height * pixelPerUnit)
    val screenSize = new Dimension(imgWidth.toInt + 2 * edge, imgHeight.toInt + 2 * edge + topHeight)
    val totalSize = new Dimension(screenSize.width * screenPixelFactor, screenSize.height * screenPixelFactor)

    val backgroundColor = Color.white

    val buffer = new BufferedImage(totalSize.width, totalSize.height, BufferedImage.TYPE_INT_ARGB)

    def clearImageBuffer(): Unit = {
      val g = buffer.getGraphics
      g.setColor(backgroundColor)
      g.fillRect(0,0, buffer.getWidth, buffer.getHeight)
    }

    val canvas = new JPanel() {
      setBackground(backgroundColor)
      setPreferredSize(screenSize)

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)

        g.drawImage(buffer, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    }

    val timePerFrame = 1.0/frameRate
    def drawAndSleep(g: Graphics, penStartFrom: Vec2): Unit = {

      var timer = 0.0
      def rest(dis: Double) = {
        timer += dis/penSpeed
        if(timer > timePerFrame){
          val millis = (timePerFrame / 0.001).toInt
          val nanos = ((timePerFrame % 0.001) * 1000000).toInt
          timer -= timePerFrame
          Thread.sleep(millis, nanos)
        }
      }
      val wordsRestDis = 5

      val screenG = g.asInstanceOf[Graphics2D]
      val imgG = buffer.getGraphics.asInstanceOf[Graphics2D]

      val penColor = Color.black

      result.words.foreach {
        case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
          val painter = new LetterPainter(screenG, pixelPerUnit = pixelPerUnit, displayPixelScale = 1,
            imageOffset = Vec2(edge, edge + topHeight), dotsPerUnit = dotsPerUnit, thicknessScale = 1.8)

          painter.drawAndBuffer(screenPixelFactor, imgG, rest)(mainSegs++secondSegs, offset, penColor)
          rest(wordsRestDis)
      }
    }

    val startButton = new JButton("Start")
    MyButton.addAction(startButton, () => {
      startButton.setEnabled(false)
      new Thread(new Runnable {
        override def run(): Unit = {
          clearImageBuffer()
          canvas.repaint()
          drawAndSleep(canvas.getGraphics, Vec2.zero)
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


  def showInScrollPane(result: RenderingResult, dotsPerUnit: Double, pixelPerUnit: Double, screenPixelFactor: Int): JScrollPane = {
    new JScrollPane(new JPanel() {
      setBackground(Color.white)

      val edge = (pixelPerUnit * 2).toInt
      val topHeight = (2*pixelPerUnit).toInt
      val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit , result.height * pixelPerUnit)
      val screenSize = new Dimension(imgWidth.toInt + 2 * edge, imgHeight.toInt + 2 * edge + topHeight)
      val totalSize = new Dimension(screenSize.width * screenPixelFactor, screenSize.height * screenPixelFactor)

      setPreferredSize(screenSize)

      def drawToGraphics(g: Graphics): Unit = {
        val g2d = g.asInstanceOf[Graphics2D]

        val color = Color.black

        result.words.foreach {
          case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
            val painter = new LetterPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = screenPixelFactor,
              imageOffset = Vec2(edge, edge + topHeight), dotsPerUnit = dotsPerUnit, thicknessScale = 1.8)

            painter.draw(mainSegs++secondSegs, offset, color)
        }
      }

      val img = new BufferedImage(totalSize.width, totalSize.height, BufferedImage.TYPE_INT_ARGB)
      drawToGraphics(img.getGraphics)

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)

        g.drawImage(img, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    })

  }
}
