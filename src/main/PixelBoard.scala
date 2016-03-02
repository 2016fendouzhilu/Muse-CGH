package main

import java.awt.Color
import java.awt.image.BufferedImage

import utilities.Vec2


/**
 * Created by weijiayi on 2/28/16.
 */
class PixelBoard(val width: Int, val height: Int, val baseLine: Int, fontColor: Color, val pixelPerUnit: Double) {
  val size = width*height
  private val pixels = new Array[Double](size)

  def setPixel(x: Int, y: Int, value: Double): Unit ={
    val index = x + (y+baseLine)*width
    assert(index < size)
    pixels(index) = value
  }

  def addToPixel(x: Int, y: Int, delta: Double): Unit ={
    val index = x + (y+baseLine)*width
    assert(index < size)
    pixels(index) += delta
  }

  def dropInk(posInUnit: Vec2, radiusInUnit: Double, color: Double): Unit ={
    val pos = posInUnit*pixelPerUnit
    val radius = radiusInUnit*pixelPerUnit

    val minX = math.max((pos.x-radius).toInt, 0)
    val maxX = math.min((pos.x+radius).ceil.toInt,width-1)
    val minY = math.max((pos.y-radius).toInt,-baseLine)
    val maxY = math.min((pos.y+radius).ceil.toInt,height-baseLine-1)

    for{
      x <- minX to maxX
      y <- minY to maxY
    }{
      val center = Vec2(x+0.5, y+0.5)
      val dis = (center - pos).length
      val ratio =
        if(dis<radius-0.5) 1.0
        else if (dis>radius+0.5) 0.0
        else radius+0.5 - dis

      addToPixel(x,y, color*ratio)
    }

  }

  def drawLine(from: Vec2, to: Vec2, startWidth: Double, endWidth: Double, dots: Int, color: Double): Unit ={
    val delta = to-from
    val length = delta.length

    val offset = delta/dots
    val deltaR = (endWidth - startWidth)/dots

    for(i <- 0 until dots){
      val r = startWidth + i * deltaR
      val center = from + offset * i
      dropInk(center, r, color)
    }
  }

  def drawCurve(inkCurve: InkCurve, color: Double): Unit = inkCurve match{
    case InkCurve(curve, dots, start, end) =>
      val dt = 1.0/dots
      val deltaR = (end-start)/dots

      for(i <- 0 until dots){
        val t = i*dt
        val p = curve.eval(t)
        val r = start + i * deltaR
        dropInk(p, r, color)
      }
  }

  def drawLetter(letter: Letter, color: Double): Unit = letter.segs.foreach(c => drawCurve(c, color))

  def grayToInt(gray: Double): Int = {
    val g = if(gray > 1) 1.0 else gray
    val int = (g*255).toInt
    new Color(fontColor.getRed, fontColor.getGreen, fontColor.getBlue, int).getRGB
  }

  def toImage ={
    val img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB)
    val ints = pixels.map(grayToInt)
    img.setRGB(0,0,width,height,ints, 0 ,width)
    img
  }

}
