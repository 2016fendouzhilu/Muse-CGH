package utilities

import java.nio.file.Paths

import main.Letter

/**
 * Created by weijiayi on 3/5/16.
 */
object LetterMapLoader {

  def loadDefaultLetterMap(): Map[Char, Letter] = {
    var list = List[(Char, Letter)]()

    println("letters missing: ")

    (0 until 26).foreach{ i =>
      val c = ('a'.toInt + i).toChar
      loadLetter(s"letters/$c.muse") match{
        case Some(l) =>
          list = List(c -> l, c.toUpper -> l) ++ list
        case None =>
          print(s"$c ")
      }
    }

    List(
      ','->"comma",
      '.'->"period",
      ';'->"semicolon",
      '\''->"upper_comma",
      '’' -> "upper_comma",
      '-'->"hyphen",
      '—' -> "hyphen"
    ).foreach{
      case (key, name) =>
        loadLetter(s"letters/$name.muse") match{
          case Some(l) => list = (key -> l) :: list
          case None =>
            print(s"$name")
        }
    }

    list.toMap
  }

  def loadLetter(fileName: String): Option[Letter] = {
    val file = Paths.get(fileName).toFile
    if(file.exists()){
      EditingSaver.loadFromFile(file).foreach{ e =>
        return Some(e.letter)
      }
    }
    None
  }
}
