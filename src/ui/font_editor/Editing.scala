package ui.font_editor

import main.MuseChar




case class Editing(letter: MuseChar, selects: Seq[Int]) {
  def selectedInkCurves = letter.getCurves(selects)
}

object Editing{
  def empty = Editing(MuseChar.empty, Seq())
}

class EditingHistory(init: Editing) {
  private var history: List[Editing] = List(init)
  private var redoBuffer: List[Editing] = List()

  def addHistory(e: Editing): Unit ={
    history = e :: history
    redoBuffer = List()
  }

  def undo(): Editing = history match{
    case now::last::_ =>
      history = history.tail
      redoBuffer = now :: redoBuffer
      last
    case _ => history.head
  }

  def redo(): Option[Editing] = redoBuffer match{
    case h::t =>
      history = h :: history
      redoBuffer = t
      Some(h)
    case _ => None
  }
}