package command_line

/**
 * Created by weijiayi on 4/17/16.
 */
object CommandLineExample {
  def main(args: Array[String]) {

    CommandLineMain.main(Array[String](
      "LICENSE.txt", // use the content of our license file as input
      "--out", "sampleResult.png", // specify the name and format of output image
      "--LineWidth", "80", // set LineWidth to 80.0 unit
      "--AspectRatio", "1.4142" //instead of printing all the results on a very lone image, set the aspect ratio to a normal A4 paper.
    ))

    // If everything goes well, you should see the result in sampleResult.png.
  }
}
