package utilities

import editor.Editing
import main.{Letter, LetterSeg}
import utilities.MapWriter.MapData

/**
  * Created by weijiayi on 3/3/16.
  */
trait MapWriter[A] {
  def toMapData(v: A): MapData

  def fromMapData(data: MapData): A
}

object MapWriter {
  type MapData = Map[MapKey.Value, Any]

  def write[A](value: A)(implicit mapWriter: MapWriter[A]) = mapWriter.toMapData(value)

  def readOption[A](data: MapData)(implicit mapWriter: MapWriter[A]): Option[A] = {
    try{
      Some(mapWriter.fromMapData(data))
    } catch{
      case e: Throwable => None
    }
  }

  import MapKey._

  implicit val Vec2MapWriter = new MapWriter[Vec2] {
    override def toMapData(v: Vec2): MapData = Map((Vec2X, v.x),(Vec2Y, v.y))

    override def fromMapData(data: MapData): Vec2 = {
      Vec2(data(Vec2X).asInstanceOf[Double], data(Vec2Y).asInstanceOf[Double])
    }
  }

  implicit val CubicCurveWriter = new MapWriter[CubicCurve] {
    override def toMapData(v: CubicCurve): MapData = Map(
      (P0, v.p0), (P1, v.p1), (P2, v.p2), (P3, v.p3)
    )

    override def fromMapData(data: MapData): CubicCurve = CubicCurve(
      data(P0).asInstanceOf[Vec2],
      data(P1).asInstanceOf[Vec2],
      data(P2).asInstanceOf[Vec2],
      data(P3).asInstanceOf[Vec2]
    )
  }

  implicit val LetterSegWriter = new MapWriter[LetterSeg] {
    override def toMapData(v: LetterSeg): MapData = v match{
      case LetterSeg(curve, start, end, align, isBreak) => Map(
        (Curve, curve), (StartWidth, start), (EndWidth, end),
        (AlignTangent, align), (IsStrokeBreak, isBreak)
      )
    }

    override def fromMapData(data: MapData): LetterSeg = LetterSeg(
      data(Curve).asInstanceOf[CubicCurve],
      data(StartWidth).asInstanceOf[Double], data(EndWidth).asInstanceOf[Double],
      data(AlignTangent).asInstanceOf[Boolean], data(IsStrokeBreak).asInstanceOf[Boolean]
    )
  }

  implicit val LetterWriter = new MapWriter[Letter] {
    override def toMapData(v: Letter): MapData = v match {
      case Letter(segs, s, e) => Map(
        (Segs, segs.toVector), (StartX, s), (EndX, e)
      )
    }

    override def fromMapData(data: MapData): Letter = {
      Letter(data(Segs).asInstanceOf[Vector[LetterSeg]])
    }
  }

  implicit val EditingWriter = new MapWriter[Editing] {
    override def toMapData(v: Editing): MapData = v match{
      case Editing(l, selects) => Map(
        (EditingLetter, l), (EditingSelects, selects.toList)
      )
    }

    override def fromMapData(data: MapData): Editing = Editing(
      data(EditingLetter).asInstanceOf[Letter],
      data(EditingSelects).asInstanceOf[List[Int]]
    )
  }
}

@SerialVersionUID(-793964942612952737L)
object MapKey extends Enumeration{
  // CAUTION: DO NOT delete these keys, cause this may change their values and break compatibility.
  val Vec2X, Vec2Y = Value
  val P0, P1, P2, P3 = Value
  val Curve, Dots, StartWidth, EndWidth, AlignTangent, IsStrokeBreak = Value
  val Segs, Width, Tall, Deep = Value
  val EditingLetter, EditingSelects = Value
  val StartX, EndX = Value
}