package tests

import org.scalacheck.Gen
import utilities.{CubicCurve, Vec2}
import org.scalacheck.Prop.forAll

/**
  * Created by weijiayi on 3/4/16.
  */
object LetterTests {
  val vec2Gen = for{
    x <- Gen.choose(0.0,2.0)
    y <- Gen.choose(-2.0,2.0)
  } yield Vec2(x,y)

  val cubicGen = for{
    p0 <- vec2Gen
    p1 <- vec2Gen
    p2 <- vec2Gen
    p3 <- vec2Gen
  } yield CubicCurve(p0,p1,p2,p3)

  val samplesTest = forAll(cubicGen){ c =>
    val samples = c.samples(50)
    samples.head =~= c.p0 && samples.last =~= c.p3
  }

  def main(args: Array[String]) {
    samplesTest.check
  }
}
