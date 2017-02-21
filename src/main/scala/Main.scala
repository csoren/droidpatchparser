import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import droid.Patch
import play.api.libs.json.{JsArray, Json}

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
    import json.Serializers._

    val files = getFilesRecursively(Paths.get("DroidEdit"), ".drp")
    val patches = files.flatMap(Patch.load).sortBy(v => v.name)

    System.out.println(Json.prettyPrint(Json.toJson(patches)))

    System.out.println(patches.filter(_.tags.length >= 2).map(_.tags.mkString("-")).toSet)
  }

}
