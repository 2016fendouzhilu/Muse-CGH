package editor

/**
  * Current Editing Modes
  */
sealed trait EditMode

case object MoveCamera extends EditMode

case class EditControlPoint(pid: Int) extends EditMode

case class EditThickness(isHead: Boolean) extends EditMode{
  def pid = if(isHead) 0 else 3
}

case object ScaleLetter extends EditMode

case object TranslateLetter extends EditMode

case object ScaleTotalThickness extends EditMode