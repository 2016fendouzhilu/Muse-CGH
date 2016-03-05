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
    val word = "abcdefghijklmnthu"
    val beautiful = "beautiful"
    val lean = 0.25
    val renderer = new LetterRenderer(letterSpacing = 0.0)
    val letterMap = loadDefaultLetterMap()

    val letters = word.collect{
      case c if letterMap.contains(c) => letterMap(c)
    }

    val fancy = renderer.renderAWord(Vec2.zero, lean, letters)
    val plain = renderer.renderAWordPlainly(Vec2(0,4), lean, letters)

    val frame = new JFrame("Rendering Result"){
      setContentPane(new JPanel(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        setPreferredSize(new Dimension(1000,400))
        setBackground(Color.white)

        override def paintComponent(g: Graphics): Unit = {
          super.paintComponent(g)
          val g2d = g.asInstanceOf[Graphics2D]

          val painter = new LetterPainter(g2d, pixelPerUnit = 40, displayPixelScale = 1,
            imageOffset = Vec2(40,150), dotsPerUnit = 20, thicknessScale = 1.5)
          painter.draw(fancy.rSegs, Color.black)
          painter.draw(plain.rSegs, Color.black)
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
