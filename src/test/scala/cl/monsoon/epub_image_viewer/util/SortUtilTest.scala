package cl.monsoon.epub_image_viewer.util

import cats.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SortUtilTest extends AnyFunSuite with Matchers {
  test(
    "the book names follow the convention declared in the sort method " +
      "can be sorted by it's book volume number"
  ) {
    val bookNames = Seq(
      "Absalom, Absalom! 1",
      "Absalom, Absalom! 10",
      "Absalom, Absalom! 03",
      "Absalom, Absalom! 2"
    )
    val sortedBookNames = Seq(
      "Absalom, Absalom! 1",
      "Absalom, Absalom! 2",
      "Absalom, Absalom! 03",
      "Absalom, Absalom! 10"
    )

    SortUtil.sortForASeriesThings(bookNames) should contain theSameElementsAs sortedBookNames
  }

  test(
    "the book names don't follow the convention declared in the sort method" +
      "can be sorted in lexicographic order"
  ) {
    val bookNames = Seq(
      "4 Absalom, Absalom!",
      "1 Absalom, Absalom!",
      "2 Dance Dance Dance",
      "0 Dance Dance Dance"
    )
    val sortedBookNames = Seq(
      "0 Dance Dance Dance",
      "1 Absalom, Absalom!",
      "2 Dance Dance Dance",
      "4 Absalom, Absalom!"
    )

    SortUtil.sortForASeriesThings(bookNames) should contain theSameElementsAs sortedBookNames
  }

  test(
    "the book names follow the convention declared in this sort method " +
      "can be sorted by it's book volume number" +
      "and the book names don't follow the convention declared in this sort method" +
      "can be sorted in lexicographic order and all placed after the former"
  ) {
    val bookNames = Seq(
      "4 Absalom, Absalom!",
      "1 Absalom, Absalom!",
      "Absalom, Absalom! 1",
      "Absalom, Absalom! 2",
      "2 Dance Dance Dance",
      "0 Dance Dance Dance",
      "Absalom, Absalom! 10",
      "Absalom, Absalom! 03"
    )
    val sortedBookNames = Seq(
      "Absalom, Absalom! 1",
      "Absalom, Absalom! 2",
      "Absalom, Absalom! 03",
      "Absalom, Absalom! 10",
      "0 Dance Dance Dance",
      "1 Absalom, Absalom!",
      "2 Dance Dance Dance",
      "4 Absalom, Absalom!"
    )

    SortUtil.sortForASeriesThings(bookNames) should contain theSameElementsAs sortedBookNames
  }
}
