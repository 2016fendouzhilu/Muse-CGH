package utilities

import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Save images to different formats
 */
object ImageSaver {
  def saveImage(image: RenderedImage, path: String): Option[String] ={
    try {
      val (name, ext) = nameAndExtension(path, "png")
      val file = new File(name+s".$ext")
      ImageIO.write(image, ext, file)
      Some(file.getAbsolutePath)
    }catch {
      case e: Throwable =>
        println(s"failed to save image with error: $e")
        None
    }
  }

  def nameAndExtension(s: String, defaultExt:String) = {
    val names = s.split("\\.")
    if(names.length == 1){
      (names.head, defaultExt)
    }else{
      (names(names.length-2), names.last)
    }
  }
}
