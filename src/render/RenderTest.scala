package render

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics, Graphics2D, RenderingHints}
import java.io.File
import java.nio.file.Paths
import javax.swing.{JScrollPane, JPanel, JFrame}

import main.Letter
import utilities.{LetterMapLoader, EditingSaver, Vec2}

/**
  * Created by weijiayi on 3/4/16.
  */
object RenderTest {

  def renderText() = {
    val renderer = new LetterRenderer(letterSpacing = 0.0, spaceWidth = 0.8, symbolFrontSapce = 0.2)
    val letterMap = LetterMapLoader.loadDefaultLetterMap()

    val text = "None of this had even a hope of any practical application in my life. But ten years later, when we were designing the first Macintosh computer, it all came back to me. And we designed it all into the Mac. It was the first computer with beautiful typography. If I had never dropped in on that single course in college, the Mac would have never had multiple typefaces or proportionally spaced fonts. And since Windows just copied the Mac, its likely that no personal computer would have them. If I had never dropped out, I would have never dropped in on this calligraphy class, and personal computers might not have the wonderful typography that they do. Of course it was impossible to connect the dots looking forward when I was in college. But it was very, very clear looking backwards ten years later."

    renderer.renderText(letterMap, lean = 0.3, maxLineWidth = 75, breakWordThreshold = 35, lineSpacing = 4)(text)
  }

  def showInScrollPane(result: RenderingResult, dotsPerUnit: Double): Unit = {
    val frame = new JFrame(s"Rendering Result [samples = $dotsPerUnit]"){
      setContentPane(new JScrollPane(new JPanel(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        setBackground(Color.white)

        val pixelPerUnit = 28
        val displayPixelScale = 1

        val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit * displayPixelScale, result.height * pixelPerUnit * displayPixelScale)
        val edge = 40

        val screenPixelFactor: Int = 2
        val totalSize = new Dimension(imgWidth.toInt+2*edge,imgHeight.toInt+2*edge+120)
        val screenSize = new Dimension(totalSize.width/screenPixelFactor, totalSize.height/screenPixelFactor)
        setPreferredSize(screenSize)

        def drawToGraphics(g: Graphics): Unit = {
          super.paintComponent(g)
          val g2d = g.asInstanceOf[Graphics2D]
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

          val color = Color.black

          result.words.foreach {
            case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
              val painter = new LetterPainter(g2d, pixelPerUnit = pixelPerUnit, displayPixelScale = displayPixelScale,
                imageOffset = Vec2(edge,edge+60), dotsPerUnit = dotsPerUnit, thicknessScale = 1.8)

              painter.draw(mainSegs, offset, color)
              painter.draw(secondSegs, offset, color)
          }
        }

        val img = new BufferedImage(totalSize.width*2, totalSize.height*2, BufferedImage.TYPE_INT_ARGB)
        drawToGraphics(img.getGraphics)

        override def paintComponent(g: Graphics): Unit = {
          super.paintComponent(g)
          val g2d = g.asInstanceOf[Graphics2D]
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

          g2d.drawImage(img,0,0,screenSize.width,screenSize.height,0,0,totalSize.width,totalSize.height, null)
        }
      }))
      pack()
      setVisible(true)
    }
  }

  def main(args: Array[String]) {
    val result = renderText()
    val dotsPerUnit = 50.0

    showInScrollPane(result, dotsPerUnit)
  }
}
