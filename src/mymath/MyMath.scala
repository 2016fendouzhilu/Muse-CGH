package mymath

/**
  * Created by weijiayi on 2/29/16.
  */
object MyMath {
  def ceil(x: Double) = x.ceil.toInt

  def wrap(i: Int, max: Int) = {
    val x = i % max
    if(x<0) x+max else x
  }
}
