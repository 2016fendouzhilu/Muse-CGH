package utilities

import java.nio.file.Paths

import main.Letter

/**
 * Created by weijiayi on 3/5/16.
 */
object LetterMapLoader {

  def loadDefaultLetterMap(): Map[Char, Letter] = {
    var list = List[(Char, Letter)]()

    print("letters missing: ")

    (0 until 26).foreach{ i =>
      val c = ('a'.toInt + i).toChar
      val lower = loadLetter(s"letters/$c.muse")
      val upper = loadLetter(s"letters/upper_$c.muse")

      lower match {
        case Some(l) =>
          def fromLower() = {
            print(s"${c.toUpper} ")
            l
          }
          list = List(c -> l, c.toUpper -> upper.getOrElse(fromLower())) ++ list
        case None =>
          print(s"$c ${c.toUpper} ")
      }
    }

    val numberList = (0 to 9).map(i => (i + '0').toChar -> i.toString).toList
    val punctuationMarkList = List(
      ','->"comma",
      '.'->"period",
      ';'->"semicolon",
      '\''->"upper_comma",
      '’' -> "upper_comma",
      '-'->"hyphen",
      '—' -> "hyphen",
      ':' -> "colon",
      '?' -> "question_mark",
      '!' -> "exclamation_mark",
      '(' -> "open_bracket",
      ')' -> "close_bracket",
      '"' -> "quotation_mark"
    )

    (numberList ++ punctuationMarkList).foreach{
      case (key, name) =>
        loadLetter(s"letters/$name.muse") match{
          case Some(l) => list = (key -> l) :: list
          case None =>
            print(s"$name ")
        }
    }

    println("\n-----")

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
