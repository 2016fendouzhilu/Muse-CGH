package utilities

import java.io._

import editor.Editing
import utilities.MapWriter.MapData

/**
  * Created by weijiayi on 3/3/16.
  */
object EditingSaver {
  def saveToFile(file: File, editing: Editing): Unit = {
    val data = MapWriter.write(editing)
    val s = new ObjectOutputStream(new FileOutputStream(file))
    try{
      s.writeObject(data)
    }finally{
      s.close()
    }
  }

  def loadFromFile(file: File): Option[Editing] = {
    val s = new ObjectInputStream(new FileInputStream(file))
    try{
      val data = s.readObject().asInstanceOf[MapData]
      MapWriter.readOption[Editing](data)
    } catch {
      case e: Throwable =>
        println(s"failed to load: $e")
        None
    } finally{
      s.close()
    }
  }
}
