package utilities

import java.io._

import editor.Editing
import utilities.MapWriter.MapData

/**
  * Created by weijiayi on 3/3/16.
  */
object EditingSaver {
  def saveToFile(select: String, editing: Editing): Unit = {
    val musePath = if(select.endsWith(".muse")) select else select+".muse"
    val file = new File(musePath)
    val s = new ObjectOutputStream(new FileOutputStream(file))
    val textWriter = new FileWriter(musePath.replace(".muse",".txt"))
    try {
      if (file.exists()) {
        file.renameTo(new File(musePath + ".old")) // buffer the old file
      }
      val data = MapWriter.write(editing)
      s.writeObject(data)
      textWriter.write(editing.toString)
    } catch {
      case e: Throwable => println(s"failed to save file: $e")
    }finally{
      s.close()
      textWriter.close()
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
