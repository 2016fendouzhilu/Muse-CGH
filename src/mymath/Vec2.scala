package mymath

import java.awt.geom.Point2D

case class Vec2(x:Double, y:Double) extends Point2D{
  def rotate(rad: Double) = {
    val sin=math.sin(rad)
    val cos=math.cos(rad)
    this.complexTimes(Vec2(cos,sin))
  }

  def complexTimes(v: Vec2):Vec2={
    Vec2(x*v.x-y*v.y,x*v.y+y*v.x)
  }

  def *(d: Double) = Vec2(x*d,y*d)

  def /(d:Double)={
    val inv=1.0/d
    this * inv
  }

  def +(v:Vec2)=Vec2(x+v.x,y+v.y)

  def -(v:Vec2)=Vec2(x-v.x,y-v.y)

  def dot(v:Vec2)= x*v.x+y*v.y

  def lengthSquared = x*x+y*y

  def length=math.sqrt(lengthSquared)

  def normalized = this/length

  override def setLocation(x: Double, y: Double): Unit = throw new Exception("can't set")

  override def getY: Double = y

  override def getX: Double = x
}

object Vec2{
  val zero = Vec2(0,0)
  val up = Vec2(0,-1)
  val down = Vec2(0,1)
  val left = Vec2(-1,0)
  val right = Vec2(1,0)
}