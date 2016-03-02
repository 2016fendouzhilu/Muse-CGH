package editor

/**
  * Created by weijiayi on 3/1/16.
  */
sealed trait EditMode

case object MoveCamera extends EditMode

case class EditControlPoint(id: Int) extends EditMode
