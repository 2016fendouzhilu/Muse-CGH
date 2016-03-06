package utilities

/**
 * Created by weijiayi on 3/6/16.
 */
trait ChangeSource {
  private val listeners =  scala.collection.mutable.ListBuffer[ChangeListener]()

  def addListener(l: ChangeListener) = {
    listeners += l
    l.editingUpdated()
  }

  def notifyListeners() = listeners.foreach(_.editingUpdated())
}

trait ChangeListener {
  def editingUpdated(): Unit
}