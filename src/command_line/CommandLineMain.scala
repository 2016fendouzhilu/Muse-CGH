package command_line

import java.io.File
import javax.imageio.ImageIO

import gui.user.UIMain
import main.{DoubleFieldInfo, ParamsCore}
import scopt.OptionParser
import utilities.ProjectParameters

import scala.io.Source

/**
 * Created by weijiayi on 4/17/16.
 */
object CommandLineMain {
  def main(args: Array[String]) {
    if(args.isEmpty)
      UIMain.main(args)
    else{
      val core = new ParamsCore()
      var imgName = "muse_result.png"
      val parser = new OptionParser[Unit]("muse") {
        head("muse", ProjectParameters.versionNumber.toString)
        arg[String]("<input file>") foreach { ip =>
          try{
            core.textToRender.set(Source.fromFile(ip).mkString)
          }catch {
            case e: Throwable => println(s"failed to read input from file.\n$e}")
          }
        } text "the input file to read."

        opt[String]('o',"out") foreach {n => imgName = n} validate {
          n => if(n.isEmpty) failure("Option --out must not be empty") else success} text
          "the out image name (if no extension specified, use .png)"
        
        (core.layoutRow ++ core.fontRow ++ core.wordRow ++ core.randomRow).foreach{
          case DoubleFieldInfo(settable, name, constraint, description) =>
            val abbr = toAbbreviateString(name)
            val requirements = s"$name --$abbr"+constraint.requirementString
            opt[Double](abbr) foreach {settable.set} validate {d =>
              if(constraint.f(d)) success else failure(requirements)
            } text s"$name: $description, ${constraint.requirementString} (default: ${settable.get})"
        }
      }

      if(parser.parse(args)){
        println("arguments parsed")

        renderToImage(core, imgName)
      }else{
        println("bad arguments")
      }
    }
  }

  def toAbbreviateString(fullName: String): String = {
    val words = fullName.split(' ')
    words.map(w => w.head.toString.capitalize + w.tail).mkString
  }

  def renderToImage(core: ParamsCore,imgFileFullName: String): Unit = {

    val display = core.renderingResultDisplay(info=>println(info))
    println("start to paint text...")
    display.drawToBuffer()
    println("painting finished.")

    val (name, ext) = nameAndExtension(imgFileFullName, "png")
    val file = new File(name+s".$ext")
    ImageIO.write(display.buffer, ext, file)
    println(s"result saved to ${file.getAbsolutePath}")
  }


  def nameAndExtension(s: String, defaultExt:String) = {
    val (n,ext) = s.splitAt(s.lastIndexOf('.'))
    (n, if(ext.isEmpty || ext == ".") defaultExt else ext.tail)
  }

}
