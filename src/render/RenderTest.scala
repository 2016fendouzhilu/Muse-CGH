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

//    val p = showInAnimation(result, dotsPerUnit, pixelPerUnit, penSpeed = 18 )
    val p = showInScrollPane(result, dotsPerUnit, pixelPerUnit)

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

    val text = "None of this had even a hope of any practical application in my life. But ten years later, when we were designing the first Macintosh computer, it all came back to me. And we designed it all into the Mac. It was the first computer with beautiful typography. If I had never dropped in on that single course in college, the Mac would have never had multiple typefaces or proportionally spaced fonts. And since Windows just copied the Mac, its likely that no personal computer would have them. If I had never dropped out, I would have never dropped in on this calligraphy class, and personal computers might not have the wonderful typography that they do. Of course it was impossible to connect the dots looking forward when I was in college. But it was very, very clear looking backwards ten years later."

    renderer.renderTextInParallel(letterMap, lean = 0.3, maxLineWidth = 75, breakWordThreshold = 35, lineSpacing = 4)(text)
  }

  def showInAnimation(result: RenderingResult, dotsPerUnit: Double,
                      pixelPerUnit: Double, penSpeed: Double): JPanel = {

    val edge = (pixelPerUnit * 2).toInt
    val (imageWidth, imageHeight) = ((result.lineWidth * pixelPerUnit).toInt, (result.height * pixelPerUnit).toInt)
    val totalSize = new Dimension(imageWidth + 2 * edge, imageHeight + 2 * edge + 120)

    def drawAndSleep(g: Graphics, penSpeed: Double, penStartFrom: Vec2): Unit = {

      var lastPos = penStartFrom

      def rest(v: Vec2) = {
        val dis = (v - lastPos).length

        lastPos = v
        val dt = dis / penSpeed
        val millis = (dt / 0.001).toInt
        val nanos = ((dt % 0.001) * 1000000).toInt
        Thread.sleep(millis, nanos)
      }

      val g2d = g.asInstanceOf[Graphics2D]

      val color = Color.black

      result.words.foreach {
        case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
          val painter = new LetterPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = 1,
            imageOffset = Vec2(edge, edge + 60), dotsPerUnit = dotsPerUnit, thicknessScale = 1.8)

          painter.draw(mainSegs, offset, color, rest)
          painter.draw(secondSegs, offset, color, rest)
      }
    }

    val canvas = new JPanel() {
      setBackground(Color.white)
      setPreferredSize(totalSize)
    }

    val startButton = new JButton("Start")
    MyButton.addAction(startButton, () => {
      startButton.setEnabled(false)
      new Thread(new Runnable {
        override def run(): Unit = {
          canvas.repaint()
          drawAndSleep(canvas.getGraphics, penSpeed, Vec2.zero)
          startButton.setEnabled(true)
        }
      }).start()

    })

    new JPanel() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      add(startButton)
      add(canvas)
    }
  }


  def showInScrollPane(result: RenderingResult, dotsPerUnit: Double, pixelPerUnit: Double): JScrollPane = {
    new JScrollPane(new JPanel() {
      setBackground(Color.white)

      val screenPixelFactor: Int = 2
      val edge = (screenPixelFactor * pixelPerUnit * 2).toInt
      val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit * screenPixelFactor, result.height * pixelPerUnit * screenPixelFactor)
      val totalSize = new Dimension(imgWidth.toInt + 2 * edge, imgHeight.toInt + 2 * edge + 120)
      val screenSize = new Dimension(totalSize.width / screenPixelFactor, totalSize.height / screenPixelFactor)

      setPreferredSize(screenSize)

      def drawToGraphics(g: Graphics): Unit = {
        val g2d = g.asInstanceOf[Graphics2D]

        val color = Color.black

        result.words.foreach {
          case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
            val painter = new LetterPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = screenPixelFactor,
              imageOffset = Vec2(edge, edge + 60), dotsPerUnit = dotsPerUnit, thicknessScale = 1.8)

            painter.draw(mainSegs, offset, color)
            painter.draw(secondSegs, offset, color)
        }
      }

      val img = new BufferedImage(totalSize.width, totalSize.height, BufferedImage.TYPE_INT_ARGB)
      drawToGraphics(img.getGraphics)

      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        val g2d = g.asInstanceOf[Graphics2D]
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.drawImage(img, 0, 0, screenSize.width, screenSize.height, 0, 0, totalSize.width, totalSize.height, null)
      }
    })

  }
}
