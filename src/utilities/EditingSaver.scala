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
    try{
      val file = new File(musePath)
      if (file.exists()) {
        val bufferFile = new File(musePath + ".old")
        if(bufferFile.exists()) bufferFile.delete()
        file.renameTo(bufferFile) // buffer the old file
      }
    }catch {
      case e: Throwable => println(s"can't buffer .muse file: $e")
    }

    val outputStream = new ObjectOutputStream(new FileOutputStream(musePath))
    val textWriter = new FileWriter(musePath.replace(".muse",".txt"))
    try {
      val data = MapWriter.write(editing)
      outputStream.writeObject(data)
      textWriter.write(editing.toString)
    } catch {
      case e: Throwable => println(s"failed to save .muse file: $e")
    }finally{
      outputStream.close()
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
