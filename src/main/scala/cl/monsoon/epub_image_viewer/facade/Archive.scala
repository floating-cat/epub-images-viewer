package cl.monsoon.epub_image_viewer.facade

import cats.implicits._
import org.scalajs.dom.File

import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.chaining._

@JSImport("libarchive.js/main.js", "Archive")
@js.native
class Archive(@unused file: File, @unused options: Options) extends js.Object {
  def open(): js.Promise[Archive] = js.native

  def extractFiles(): js.Promise[js.Dynamic] = js.native
}

trait Options extends js.Object {
  val workerUrl: String
}

object Archive {

  def extractZip(file: File)(implicit executor: ExecutionContext): Future[String => Option[File]] =
    new Archive(
      file,
      new Options {
        override val workerUrl: String = "worker-bundle.js"
      }
    ).open()
      .toFuture
      .flatMap(_.extractFiles().toFuture)
      .map(getFileSupplier)

  private def getFileSupplier(filesObject: js.Dynamic): String => Option[File] = { filePath =>
    val paths = if (filePath != null) {
      filePath.split("/").toList
    } else {
      List.empty
    }

    paths
      .foldM(filesObject) { (nestFilesObject, path) =>
        nestFilesObject
          .selectDynamic(path)
          .pipe(nestFilesObject => Option.unless(js.isUndefined(nestFilesObject))(nestFilesObject))
      }
      .collect { case o if o.isInstanceOf[File] => o.asInstanceOf[File] }
  }
}
