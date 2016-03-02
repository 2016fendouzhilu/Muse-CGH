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

  /**
    * calculate the new index in an array
    * @param index current pos
    * @param size array size
    * @param offset index offset
    * @return
    */
  def nearIndexOption(index: Int, size: Int, offset: Int): Option[Int] = {
    val ni = index + offset
    if(ni>=0 && ni<size)
      Some(ni)
    else
      None
  }
}
