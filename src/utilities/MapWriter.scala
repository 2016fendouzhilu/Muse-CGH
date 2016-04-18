package utilities

import gui.font_editor.Editing
import main.{MuseChar, LetterSeg, MuseCharType}


@SerialVersionUID(1L)
abstract class MapData extends Serializable

@SerialVersionUID(1L)
case class IntMap(data: Map[Int, MapData]) extends MapData{
  def getDouble(key: Int) = data(key) match {
    case DataDouble(d) => d
  }

  def getInt(key: Int) = data(key) match {
    case DataInt(d) => d
  }

  def getBool(key: Int) = data(key) match {
    case DataBool(d) => d
  }

  def getArray(key: Int) = data(key) match {
    case DataArray(a) => a
  }

  def apply(key: Int) = data(key).asInstanceOf[IntMap]

  def hasKey(key: Int) = data.contains(key)
}

@SerialVersionUID(1L)
case class DataDouble(d: Double) extends MapData

@SerialVersionUID(1L)
case class DataInt(i: Int) extends MapData

@SerialVersionUID(1L)
case class DataBool(b: Boolean) extends MapData

@SerialVersionUID(1L)
case class DataArray(a: IndexedSeq[MapData]) extends MapData


trait MapWriter[A] {
  def toMapData(v: A): IntMap

  def fromMapData(data: IntMap): A
}

object MapWriter {

  def write[A](value: A)(implicit mapWriter: MapWriter[A]): IntMap = mapWriter.toMapData(value)

  def readOption[A](data: IntMap)(implicit mapWriter: MapWriter[A]): Option[A] = {
    try{
      Some(read[A](data))
    } catch{
      case e: Throwable => None
    }
  }

  def read[A](data: IntMap)(implicit mapWriter: MapWriter[A]): A = {
    mapWriter.fromMapData(data)
  }

  import MapKey._

  implicit val Vec2MapWriter = new MapWriter[Vec2] {
    override def toMapData(v: Vec2): IntMap = IntMap(Map(
      Vec2X -> DataDouble(v.x), Vec2Y -> DataDouble(v.y)
    ))

    override def fromMapData(data: IntMap): Vec2 = {
      Vec2(data.getDouble(Vec2X), data.getDouble(Vec2Y))
    }
  }

  implicit val CubicCurveWriter = new MapWriter[CubicCurve] {
    override def toMapData(v: CubicCurve): IntMap = IntMap(Map(
      P0 -> write(v.p0), P1 -> write(v.p1), P2 -> write(v.p2), P3 -> write(v.p3)
    ))

    override def fromMapData(data: IntMap): CubicCurve = CubicCurve(
      read[Vec2](data(P0)),
      read[Vec2](data(P1)),
      read[Vec2](data(P2)),
      read[Vec2](data(P3))
    )
  }

  implicit val LetterSegWriter = new MapWriter[LetterSeg] {
    override def toMapData(v: LetterSeg): IntMap = v match{
      case LetterSeg(curve, start, end, align, isBreak) => IntMap(Map(
        Curve -> write(curve), StartWidth -> DataDouble(start), EndWidth -> DataDouble(end),
        AlignTangent -> DataBool(align), IsStrokeBreak -> DataBool(isBreak)
      ))
    }

    override def fromMapData(data: IntMap): LetterSeg = LetterSeg(
      read[CubicCurve](data(Curve)),
      data.getDouble(StartWidth), data.getDouble(EndWidth),
      data.getBool(AlignTangent), data.getBool(IsStrokeBreak)
    )
  }

  implicit val LetterWriter = new MapWriter[MuseChar] {
    override def toMapData(v: MuseChar): IntMap = v match {
      case MuseChar(segs, t) => IntMap(Map(
        Segs -> DataArray(segs.map(s => write(s))),
        LType -> DataInt(t.id)
      ))
    }

    override def fromMapData(data: IntMap): MuseChar = {
      val segs = data.getArray(Segs).map(d => read[LetterSeg](d.asInstanceOf[IntMap]))
      val t = if (data.hasKey(LType)) MuseCharType(data.getInt(LType)) else MuseCharType.LowerCase
      MuseChar(segs, t)
    }
  }

  implicit val EditingWriter = new MapWriter[Editing] {
    override def toMapData(v: Editing): IntMap = v match{
      case Editing(l, selects) => IntMap(Map(
        EditingLetter -> write(l),
        EditingSelects -> DataArray(selects.map(DataInt).toIndexedSeq)
      ))
    }

    override def fromMapData(data: IntMap): Editing = Editing(
      read[MuseChar](data(EditingLetter)),
      data.getArray(EditingSelects).map{ _.asInstanceOf[DataInt].i }
    )
  }
}


object MapKey {
  var id = 0
  def Value = {
    id = id + 1
    id
  }

  // CAUTION: DO NOT delete these keys, cause this may change their values and break compatibility.
  val Vec2X, Vec2Y = Value
  val P0, P1, P2, P3 = Value
  val Curve, Dots, StartWidth, EndWidth, AlignTangent, IsStrokeBreak = Value
  val Segs, Width, Tall, Deep = Value
  val EditingLetter, EditingSelects = Value
  val StartX, EndX = Value
  val LType = Value
}