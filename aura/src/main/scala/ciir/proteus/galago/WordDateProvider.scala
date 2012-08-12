package ciir.proteus.galago

import java.io.File;
import ciir.proteus._
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import org.lemurproject.galago.core.index.disk.DiskBTreeReader
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.tupleflow.VByteInput;

object WordDateProvider {
  def apply(p: Parameters) = new WordDateProvider(p)
}

class WordDateProvider(parameters: Parameters) {
  val indexFile = new File(parameters.getString("dateDirectory"), "postings")
  val index = new DiskBTreeReader(indexFile)
  printf("Opening word frequency index at %s\n", indexFile.getCanonicalPath)
  val siteId = parameters.getString("siteId")
  def lookup(word: String) : Option[LongValueList] = {
    if (index == null) return None
    val iterator = index.getIterator(Utility.fromString(word))
    if (iterator == null) return None
    val stream = iterator.getSubValueStream(0, iterator.getValueLength)
    val count = stream.readInt()
    val vStream = new VByteInput(stream)
    var dates = new ArrayBuffer[WeightedDate](count)
    for (i <- 0 until count) {
      val w = WeightedDate(date = vStream.readInt, weight = vStream.readInt.toDouble)
      dates += w
    }
    return Some(LongValueList(dates = dates.toList))
  }
}
