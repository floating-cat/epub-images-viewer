package cl.monsoon.epub_image_viewer

import cats.Show
import cats.data.NonEmptyChain
import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReaderJs.{Errors, ImageFileDataUrl}
import cl.monsoon.epub_image_viewer.facade.Archive
import cl.monsoon.epub_image_viewer.util.CustomAttributeUtil.{ariaLabel, role, webkitdirectory}
import cl.monsoon.epub_image_viewer.util.DomImplicit._
import cl.monsoon.epub_image_viewer.util.SortUtil
import org.scalajs.dom.File
import org.scalajs.dom.console.log
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.{ReactElement, SetStateHookCallback}
import slinky.web.html._
import zio._
import zio.interop.catz._

import scala.util.chaining._

@react object FilesOrDirectoriesInputView {
  type Props = Seq[ImageFileDataUrl] => Unit

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val (imageFiles, imageFilesUpdateState) = useState(Seq[File]())
    val (error, errorsUpdateState) = useState(none[String])

    bootstrapRowWrapper(
      // https://github.com/shadaj/slinky/issues/282#issuecomment-511059616
      List[ReactElement](
        div(
          className := "col-auto mb-sm-3 btn-toolbar",
          role := "toolbar",
          ariaLabel := "Toolbar with button groups"
        )(
          div(className := "btn-group", ariaLabel := "Files or directory selection buttons group")(
            input(
              `type` := "file",
              id := "files",
              className := "hidden",
              // We can add other archive formats here but we only accept .epub here
              // in order to align the directory files search behavior (because we would
              // only search .epub files when users select the directories).
              // But users can still choose ALL Files in the input dialog.
              accept := ".epub",
              multiple,
              onChange := { e =>
                addEpubFiles(e.target.files.toSeq, imageFilesUpdateState, filter = false)
              }
            ),
            label(htmlFor := "files", className := "btn btn-secondary")("Select EPUB files"),
            input(
              `type` := "file",
              id := "directories",
              className := "hidden",
              webkitdirectory := "true",
              multiple,
              onChange := { e =>
                addEpubFiles(e.target.files.toSeq, imageFilesUpdateState, filter = true)
              }
            ),
            // current multiple doesn't work with webkitdirectory
            // so we use directory prompt here
            label(htmlFor := "directories", className := "btn btn-secondary")(
              "Select a EPUB directory"
            )
          ),
          div(
            className := "input-group ml-sm-2",
            ariaLabel := "Start to view button group",
            onClick := { _ =>
              viewEpubFiles(
                imageFiles,
                props,
                (errorsUpdateState(_: Option[String])).compose(Some(_))
              )
            }
          )(
            label(className := "btn btn-success")("Start to view")
          )
        ),
        div(className := "w-100"),
        div(className := "col-lg-7")(
          error.fold(null.asInstanceOf[ReactElement])(errorString =>
            div(className := "alert alert-danger backslash-n-is-new-line", role := "alert")(
              errorString
            )
          )
        ),
        div(className := "w-100"),
        div(className := "col-lg-7")(FileNamesListView(imageFiles))
      )
    )

  }

  def bootstrapRowWrapper(children: ReactElement*): ReactElement =
    div(className := "container")(
      div(className := "row justify-content-center p-sm-4")(
        children
      )
    )

  def addEpubFiles(
      files: Seq[File],
      imageFilesUpdateState: SetStateHookCallback[Seq[File]],
      filter: Boolean
  ): Unit = {
    val sortedNewAddedEpubFiles = files
      .pipe(files => if (filter) files.filter(_.name.endsWith(".epub")) else files)
      .pipe(files => SortUtil.sortForASeriesThings(files)(Show.show(_.name)))
    imageFilesUpdateState(lastEpubFiles => lastEpubFiles ++ sortedNewAddedEpubFiles)
  }

  def viewEpubFiles(
      epubFiles: Seq[File],
      imageFileDataUrlsCallback: Props,
      errorsUpdateState: String => Unit
  ): Unit =
    epubFiles
      .map(getEpubFileImageDataUrls)
      .sequence
      .flatMap(imageDataUrls =>
        UIO.effectTotal {
          imageFileDataUrlsCallback(imageDataUrls.flatten)
        }
      )
      .flatMapError { o =>
        errorsUpdateState(o.mkString_("", "\n", ""))
        ZIO.none
      }
      .pipe(Runtime.default.unsafeRunAsync_(_))

  def getEpubFileImageDataUrls(epubFile: File): IO[Errors, Seq[ImageFileDataUrl]] =
    ZIO
      .effectTotal(log(epubFile))
      .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
      .mapError[Errors](e => NonEmptyChain(s"Can't load ${epubFile.name}: " + e.getMessage))
      .flatMap(new EpubReaderJs().getImageDataUrl.provide)
      .mapError(errors => errors.prepend(s"File ${epubFile.name}:"))
}
