package utilities


import main.LetterSeg

import scala.util.parsing.combinator.JavaTokenParsers

/**
  * Created by weijiayi on 3/3/16.
  */
object Parsing extends JavaTokenParsers{
  val vec2Parser: Parser[Vec2] =
    ("Vec2(" ~> floatingPointNumber <~ ",") ~ floatingPointNumber <~ ")" ^^ {case x~y => Vec2(x.toDouble,y.toDouble)}

  val cubicCurveParser: Parser[CubicCurve] =
    "CubicCurve(" ~> repsep(vec2Parser, ",") <~")" ^^ {case List(p0,p1,p2,p3) => CubicCurve(p0,p1,p2,p3)}

  val valueParser: Parser[Any] = (
      floatingPointNumber ^^ {_.toDouble}
    | decimalNumber ^^ {_.toInt}
    | "true" ^^ {_ => true}
    | "false" ^^ {_ => false}
    )

}






