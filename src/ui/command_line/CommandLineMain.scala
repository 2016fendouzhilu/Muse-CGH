package ui.command_line

import java.io.File
import javax.imageio.ImageIO

import ui.user.UIMain
import main.{DoubleFieldInfo, ParamsCore}
import scopt.OptionParser
import utilities.{ImageSaver, ProjectParameters}

import scala.io.Source

/**
 * The entrance of the program.
 * If no command-line arguments are given, the program will enter interactive GUI mode.
 * If at least one argument is given, the program will enter command-line mode.
 */
object CommandLineMain {
  def main(args: Array[String]) {
    if (args.isEmpty)
      UIMain.main(args) // enter interactive GUI mode
    else {
      val core = new ParamsCore()
      var imgName = "muse_result.png" // default output name
      val parser = new OptionParser[Unit]("muse") {
          head("muse", ProjectParameters.versionNumber.toString)
          arg[String]("<input file>") foreach { ip =>
            try {
              core.textToRender.set(Source.fromFile(ip).mkString) // read input from file
            } catch {
              case e: Throwable => println(s"failed to read input from file.\n$e}")
            }
          } text "the input file to read."

          opt[String]('o',"out") foreach { n => imgName = n } validate {
            n => if (n.isEmpty) failure("Option --out must not be empty") else success
          } text
            "the out image name (if no extension specified, use .png)"

          // Other settable parameters
          (core.layoutRow ++ core.fontRow ++ core.wordRow ++ core.randomRow).foreach {
            case DoubleFieldInfo(settable, name, constraint, description) =>
              val abbr = toAbbreviateString(name)
              val requirements = s"$name --$abbr" + constraint.requirementString
              opt[Double](abbr) foreach {
                settable.set
              } validate { d =>
                if (constraint.f(d)) success else failure(requirements)
              } text s"$name: $description, ${constraint.requirementString} (default: ${settable.get})"
          }
        }

      if (parser.parse(args)) {
        println("arguments parsed")

        renderToImage(core, imgName)
      } else {
        println("bad arguments")
        System.exit(-1)
      }
    }
  }

  def toAbbreviateString(fullName: String): String = {
    val words = fullName.split(' ')
    words.map(w => w.head.toString.capitalize + w.tail).mkString
  }

  def renderToImage(core: ParamsCore,imgName: String): Unit = {

    val paintable = core.getPaintableResult(println)
    println("start to paint text...")
    paintable.drawToBuffer()
    println("painting finished.")

    println("saving results...")
    ImageSaver.saveImage(paintable.buffer, imgName).foreach{ actualPath =>
      println(s"results saved to $actualPath")
    }
  }
  

}
