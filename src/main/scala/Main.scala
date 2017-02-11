import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.mutable.ArrayBuffer

object Main {
  def getFilesRecursively(root: Path, suffix: String): List[Path] = {
    val files = ArrayBuffer.empty[Path]
    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.getFileName.toString.endsWith(suffix))
          files += file
        FileVisitResult.CONTINUE
      }
    })
    files.toList
  }

  def main(args: Array[String]): Unit = {
    val files = getFilesRecursively(Paths.get("DroidEdit"), ".drp")
    files.foreach(System.out.println)
  }

}
