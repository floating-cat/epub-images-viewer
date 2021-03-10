package cl.monsoon.epub_image_viewer.util

import cats.Show
import cats.implicits._

object SortUtil {

  def sortForASeriesThings[T: Show](series: Seq[T]): Seq[T] = {
    // assume these series things names follow
    // "a_series_name volume_number(arabic_numerals_format) some_else" convention
    val regex = "(.+)(\\d+)".r
    val regexResultsPair = series
      .map(aThing => (aThing, regex.findPrefixMatchOf(aThing.show)))
      .partition(_._2.nonEmpty)

    val matchedAndSortedThngs = regexResultsPair._1.sortBy { p =>
      val regexResult = p._2.get
      (regexResult.group(1), regexResult.group(2).toInt)
    }
      .map(_._1)
    // The right part of the pair are the things in these series we can't know how to
    // sort them in this algorithm, So just sort them in lexicographic order.
    val unmatchedThings = regexResultsPair._2.map(_._1).sortBy(_.show)

    matchedAndSortedThngs ++ unmatchedThings
  }
}
