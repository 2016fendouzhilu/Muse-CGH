package editor

/**
  * Created by weijiayi on 3/1/16.
  */
sealed trait EditMode

case object MoveCamera extends EditMode

case class EditControlPoint(pid: Int) extends EditMode

case class EditThickness(isHead: Boolean) extends EditMode{
  def pid = if(isHead) 0 else 3
}
