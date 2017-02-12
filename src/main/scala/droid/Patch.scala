package droid

import java.io.RandomAccessFile
import java.nio.{ByteBuffer, ByteOrder}
import java.nio.channels.FileChannel
import java.nio.file.Path

case class Patch(name: String, tags: List[String], author: String, comment: List[String])

object Patch {
  private def upperCaseFirst(str: String): String =
    str.toList match {
      case head :: tail if head.isLower => (head.toUpper :: tail).mkString
      case _ => str
    }

  private def getAscii(buffer: ByteBuffer, chars: Int): String = {
    val array = new Array[Byte](chars)
    buffer.get(array)
    val v = array.takeWhile(_ != 0).map(b => b.toChar).mkString
    v
  }

  private def filterComment(comment: String): List[String] =
    if (comment == "2000 chars max") Nil else comment.filter(_ != '\r').split('\n').toList

  private def splitName(name: String): (String, List[String]) = {
    val optionalNameAndClass =
      if (name.endsWith(")")) {
        name.lastIndexOf("(") match {
          case -1 => None
          case parensIndex =>
            val (newName, classes) = splitName(name.substring(0, parensIndex).trim)
            Some(upperCaseFirst(newName), upperCaseFirst(name.substring(parensIndex + 1, name.length - 1).trim) :: classes)
        }
      } else {
        None
      }

    optionalNameAndClass.getOrElse((name, Nil))
  }


  def load(path: Path): Option[Patch] = {
    val channel = new RandomAccessFile(path.toString, "r").getChannel
    val map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size)
    map.order(ByteOrder.LITTLE_ENDIAN)

    if (getAscii(map, 3) == "DRP" && map.getInt() == 0) {
      val (name, clazz) = splitName(getAscii(map, 256).trim)
      val author = getAscii(map, 256)
      val comment = filterComment(getAscii(map, 2048))

      Some(Patch(name, clazz, author, comment))
    } else {
      None
    }
  }
}
