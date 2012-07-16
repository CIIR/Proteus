package controllers

import play.api.mvc.Controller
import models.Post
import collection.immutable.IndexedSeq


object Posts extends Controller {

  val chars = ('a' to 'z')

  /**
   * Generate some fake data to test out templating
   *
   * @param count
   * @return
   */

  def generateFakePosts(count: Int): List[Post] = {

      val list: IndexedSeq[Post] = 1 to count map { _ =>
        Post(generateString(10))
      }
      list.toList

  }


  private def generateString(length: Int) =  1 to length map {_ =>
    val index = scala.util.Random.nextInt(chars.length)
    chars(index)
  } mkString("")
}
