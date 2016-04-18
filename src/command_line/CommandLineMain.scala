package command_line

import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame

import main.ProjectParameters
import render._
import scopt.OptionParser
import utilities.{Settable, RNG, LetterMapLoader}

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
            core.textRendered.set(Source.fromFile(ip).mkString)
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

    val text = core.textRendered.get

    val (result, _) = {
      val renderer = new LetterRenderer(letterSpacing = core.letterSpacing.get,
        spaceWidth = core.spaceWidth.get,
        symbolFrontSpace = core.symbolFrontSpace.get)

      val rng = {
        RNG((core.seed.get * Long.MaxValue).toLong)
      }
      renderer.renderTextInParallel(core.letterMap.get, lean = core.lean.get,
        maxLineWidth = core.maxLineWidth.get,
        breakWordThreshold = core.breakWordThreshold.get,
        lineSpacing = core.lineSpacing.get,
        randomness = core.randomness.get,
        lineRandomness = core.lineRandomness.get)(text)(rng)
    }

    println(result.info)

    val useAspectRatio = {
      val as = core.aspectRatio.get
      if (as > 0) Some(as) else None
    }
    val display = new RenderingResultDisplay(result, core.samplesPerUnit.get, core.pixelPerUnit.get,
      thicknessScale = core.thicknessScale.get, screenPixelFactor = 1, useAspectRatio = useAspectRatio)
    display.drawToBuffer()

    val (name, ext) = nameAndExtension(imgFileFullName, "png")
    val file = new File(name+s".$ext")
    ImageIO.write(display.buffer, ext, file)
    println(s"rendered result to ${file.getAbsolutePath}")
  }



  def nameAndExtension(s: String, defaultExt:String) = {
    val (n,ext) = s.splitAt(s.lastIndexOf('.'))
    (n, if(ext.isEmpty || ext == ".") defaultExt else ext.tail)
  }

  def mkArgMap(args: IndexedSeq[String]): Map[String,String] = {
    val argN = args.length/2
    (0 until argN).map(i => args(2*i) -> args(2*i+1)).toMap
  }
}
