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
  
  def beforeNotify(action: =>Unit) = {
    action
    notifyListeners()
  }
  
  def newSettable[T](init: T) = {
    new Settable(init, notifyListeners)
  } 
}

trait ChangeListener {
  def editingUpdated(): Unit
}

class Settable[T](private var value: T, action: () => Unit) {
  def set(v: T) = {
    if(v!=value){
      value = v
      action()
    }
  }
  
  def get = value
}