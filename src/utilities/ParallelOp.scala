package utilities

/**
  * Created by weijiayi on 7/7/16.
  */
object ParallelOp {
  def foreach[A](xs: Array[A],parallelism: Int)(f: A=>Unit): Unit ={
    require(parallelism>0)
    val chunkSize = xs.length/parallelism
    val lastSize = xs.length - chunkSize * (parallelism-1)
    val runnables = (0 until parallelism).map{ i =>
      val startIdx = i*chunkSize
      val size = if(i==parallelism-1) lastSize else chunkSize
      val t = new Thread(new Runnable {
        override def run(): Unit = {
          (0 until size).foreach(i => f(xs(startIdx+i)))
        }
      })
      t.start()
      t
    }
    runnables.foreach(_.join())
  }
}
