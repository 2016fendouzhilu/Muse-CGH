package render

import java.awt.{Dimension, Color, Graphics2D, Graphics}
import java.io.File
import java.nio.file.Paths
import javax.swing.{JPanel, JFrame}

import main.Letter
import utilities.{EditingSaver, Vec2}

/**
  * Created by weijiayi on 3/4/16.
  */
object RenderTest {

  def main(args: Array[String]) {
    val renderer = new LetterRenderer(letterSpacing = 0.0, spaceWidth = 0.8)
    val letterMap = loadDefaultLetterMap()

    val text = "And I behold once more\nMy old familiar haunts; here the blue river,\nThe same blue wonder that my infant eye\nAdmired, sage doubting whence the traveller came,—\nWhence brought his sunny bubbles ere he washed\nThe fragrant flag-roots in my father’s fields,\nAnd where thereafter in the world he went.\nLook, here he is, unaltered, save that now\nHe hath broke his banks and flooded all the vales\nWith his redundant waves.\nHere is the rock where, yet a simple child,\nI caught with bended pin my earliest fish,\nMuch triumphing, —and these the fields\nOver whose flowers I chased the butterfly,\nA blooming hunter of a fairy fine.".toLowerCase

    val result = renderer.renderText(letterMap, lean = 0.3, maxLineWidth = 40, breakWordThreshold = 35, lineSpacing = 4)(text)

    val frame = new JFrame("Rendering Result"){
      setContentPane(new JPanel(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        setBackground(Color.white)

        val pixelPerUnit = 40
        val displayPixelScale = 1

        val (imgWidth, imgHeight) = (result.lineWidth * pixelPerUnit * displayPixelScale, result.height * pixelPerUnit * displayPixelScale)
        val edge = 20

        setPreferredSize(new Dimension(imgWidth.toInt + 2*edge,imgHeight.toInt+2*edge+120))


        override def paintComponent(g: Graphics): Unit = {
          super.paintComponent(g)
          val g2d = g.asInstanceOf[Graphics2D]

          val color = Color.black

          result.words.foreach{
            case (offset, RenderingWord(mainSegs, secondSegs, _)) =>
              val painter = new LetterPainter(g2d, pixelPerUnit = 20, displayPixelScale = 1,
                imageOffset = Vec2(edge,edge+60), dotsPerUnit = 20, thicknessScale = 1.5)

              painter.draw(mainSegs, offset, color)
              painter.draw(secondSegs, offset, color)
          }
        }
      })
      pack()
      setVisible(true)
    }
  }

  def loadDefaultLetterMap(): Map[Char, Letter] = {
    var list = List[(Char, Letter)]()

    println("letters missing: ")

    (0 until 26).foreach{ i =>
      val c = ('a'.toInt + i).toChar
      val file = Paths.get(s"letters/$c.muse").toFile

      var missing = true
      if(file.exists()){
        EditingSaver.loadFromFile(file).foreach{ e =>
          list = (c, e.letter)::list
          missing = false
        }
      }

      if(missing)
        print(s"$c ")
    }
    list.toMap
  }
}
