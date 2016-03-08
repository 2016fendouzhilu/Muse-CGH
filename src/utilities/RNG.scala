package utilities

/**
 * Functional Random number generator
 */
case class RNG(seed: Long)


object RNG {
  val nextInt: State[RNG, Int] =  (rng: RNG) => {
    val seed = rng.seed
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRNG = RNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRNG)
  }

  val nextDouble: State[RNG, Double] = nextInt.map { _.toDouble / Int.MaxValue }

  def nextDoubles(num: Int): State[RNG, IndexedSeq[Double]] = (s: RNG) => {
    var state = s
    val ds = for(_ <- 0 until num) yield {
      val (d, s1) = nextDouble(state)
      state = s1
      d
    }
    (ds, state)
  }

  implicit class State[S,V](sf: S => (V, S)){
    def apply(s: S) = sf(s)

    def flatMap[V1](f: V => State[S, V1]): State[S, V1] = State((s: S) => {
      val (v, s1) = sf(s)
      f(v)(s1)
    })

    def map[V1](f: V => V1): State[S,V1] = (s: S) => {
      val (v, s1) = sf(s)
      (f(v), s1)
    }
  }
}