package main

import java.awt.{Color, Dimension, Graphics, Graphics2D}
import javax.swing.{JFrame, JPanel}

import utilities.Vec2

/**
 * Created by weijiayi on 2/28/16.
 */
object Test {
  val imgWidth = 800
  val imgHeight = 600

  val board = new PixelBoard(imgWidth, imgHeight, imgHeight/2, Color.black, 10)
  board.dropInk(Vec2(20,20),4,0.6)
  board.dropInk(Vec2(22,20),4,0.6)

  board.drawLine(Vec2(10,10),Vec2(100,100),7,8,100,0.1)


  def main(args: Array[String]) {
    val frame = new JFrame("Test"){
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

      setContentPane(new JPanel(){
        setPreferredSize(new Dimension(imgWidth, imgHeight))

        override def paintComponent(g: Graphics): Unit = {
          val g2d = g.asInstanceOf[Graphics2D]

          g2d.drawImage(board.toImage, 0, 0, null)
        }
      })

      pack()
      setVisible(true)
    }


  }

}
