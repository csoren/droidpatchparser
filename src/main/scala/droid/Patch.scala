package droid

import java.io.RandomAccessFile
import java.nio.{ByteBuffer, ByteOrder}
import java.nio.channels.FileChannel
import java.nio.file.Path

case class Patch(name: String, clazz: String, author: String, comment: String)

object Patch {
  def getAscii(buffer: ByteBuffer, chars: Int): String = {
    val array = new Array[Byte](chars)
    buffer.get(array)
    val v = array.map(b => b.toChar).mkString
    v
  }

  def load(path: Path): Option[Patch] = {
    val channel = new RandomAccessFile(path.toString, "r").getChannel
    val map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size)
    map.order(ByteOrder.LITTLE_ENDIAN)

    if (getAscii(map, 3) == "DRP" && map.getInt() == 0) {
      val name = getAscii(map, 256)
      val author = getAscii(map, 256)
      val comment = getAscii(map, 2048)

      Some(Patch(name, "", author, comment))
    } else {
      None
    }
  }
}
