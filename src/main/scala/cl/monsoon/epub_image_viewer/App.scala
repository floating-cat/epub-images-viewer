package cl.monsoon.epub_image_viewer

import cats.data.NonEmptyChain
import cats.implicits._
import cl.monsoon.epub_image_viewer.EpubReader.{Errors, ImageFileDataUrl}
import cl.monsoon.epub_image_viewer.facade.Archive
import org.scalajs.dom.console.log
import org.scalajs.dom.raw.KeyboardEvent
import org.scalajs.dom.{document, window}
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._
import zio.{DefaultRuntime, UIO, ZIO}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.chaining._

@JSImport("resources/App.css", JSImport.Default)
@js.native
object AppCSS extends js.Object

@react object App {
  type Props = Unit

  val css: AppCSS.type = AppCSS

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { _ =>
    val (imageFileDataUrls, imageFileDataUrlsUpdateState) = useState(none[Seq[ImageFileDataUrl]])
    val (imageViewedIndex, imageViewedIndexUpdateState) = useState(0)
    val (autoFitImage, autoFitImageUpdateState) = useState(true)

    useEffect { () =>
      // Use js function explicit because of  https://stackoverflow.com/q/57148965/2331527
      val keyDownListener: js.Function1[KeyboardEvent, Unit] = { e: KeyboardEvent =>
        imageFileDataUrls.foreach { urls =>
          e.key match {
            case "ArrowLeft" =>
              if (urls.lengthIs >= imageViewedIndex + 1 + 1) {
                imageViewedIndexUpdateState(imageViewedIndex + 1)
              }
            case "ArrowRight" =>
              if (imageViewedIndex - 1 >= 0) {
                imageViewedIndexUpdateState(imageViewedIndex - 1)
              }
            case "f" => autoFitImageUpdateState(!_)
            case "e" =>
              imageFileDataUrlsUpdateState(none)
              imageViewedIndexUpdateState(0)
            case _ =>
          }
        }
      }

      val eventName = "keydown"
      document.addEventListener(eventName, keyDownListener)
      () => document.removeEventListener(eventName, keyDownListener)
    }

    val mainContent = if (imageFileDataUrls.isEmpty || imageFileDataUrls.get.isEmpty) {
      input(
        `type` := "file",
        multiple,
        onChange := { e =>
          val epubFile = e.target.files(0)
          ZIO
            .effectTotal(log(epubFile))
            .flatMap(_ => ZIO.fromFuture(ec => Archive.extractZip(epubFile)(ec)))
            .mapError[Errors](
              e => NonEmptyChain(s"Can't load ${epubFile.name}: " + e.getMessage)
            )
            .flatMap(new EpubReaderJs().getImageDataUrl.provide)
            .flatMap(
              imageDataUrls =>
                UIO.effectTotal {
                  imageFileDataUrlsUpdateState(Some(imageDataUrls))
                }
            )
            .flatMapError(o => {
              window.alert(o.mkString_("", "\n", ""))
              ZIO.none
            })
            .pipe(new DefaultRuntime {}.unsafeRunAsync_(_))
        }
      )
    } else {
      img(
        className := "illustration" + (if (autoFitImage) " auto_fit" else ""),
        src := imageFileDataUrls.get(imageViewedIndex)
      )
    }

    div(className := "App")(mainContent)
  }
}
