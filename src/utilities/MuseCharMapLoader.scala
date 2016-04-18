package utilities

import java.nio.file.Paths

import main.MuseChar

/**
 * Load MuseChars from disk
 */
object MuseCharMapLoader {

  def loadDefaultCharMap(): Map[Char, MuseChar] = {
    var list = List[(Char, MuseChar)]()

    print("letters missing: ")

    (0 until 26).foreach{ i =>
      val c = ('a'.toInt + i).toChar
      val lower = loadChar(s"letters/$c.muse")
      val upper = loadChar(s"letters/upper_$c.muse")

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
        loadChar(s"letters/$name.muse") match{
          case Some(l) => list = (key -> l) :: list
          case None =>
            print(s"$name ")
        }
    }

    println("\n-----")

    list.toMap
  }

  def loadChar(fileName: String): Option[MuseChar] = {
    val file = Paths.get(fileName).toFile
    if(file.exists()){
      EditingSaver.loadFromFile(file).foreach{ e =>
        return Some(e.letter)
      }
    }
    None
  }
}

