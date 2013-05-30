// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.parse;

import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.lemurproject.galago.core.types.WordCount;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.IncompatibleProcessorException;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.Sorter;
import org.lemurproject.galago.tupleflow.Utility;

/**
 *
 * @author trevor
 */
public class WordCounterTest extends TestCase {

  public WordCounterTest(String testName) {
    super(testName);
  }

  private static class PostStep implements WordCount.Processor {

    public ArrayList<WordCount> results = new ArrayList<WordCount>();

    public void process(WordCount o) {
      results.add((WordCount) o);
    }

    public void close() {
    }
  };

  public void testCountUnigrams() throws IOException, IncompatibleProcessorException {
    WordCounter counter = new WordCounter(new FakeParameters(new Parameters()));
    Document document = new Document();
    PostStep post = new PostStep();

    counter.setProcessor(post);

    document.terms = new ArrayList<String>();
    document.terms.add("one");
    document.terms.add("two");
    document.terms.add("one");
    counter.process(document);

    assertEquals(2, post.results.size());

    for (int i = 0; i < post.results.size(); ++i) {
      WordCount wc = post.results.get(i);
      if (Utility.compare(wc.word, Utility.fromString("one")) == 0) {
        assertEquals(2, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("one")) == 0) {
        assertEquals(1, wc.count);
      }
    }
  }

  public void testCountReducer() throws IOException, IncompatibleProcessorException {
    Parameters p = new Parameters();
    WordCounter counter = new WordCounter(new FakeParameters(p));
    Sorter sorter = new Sorter(new WordCount.WordOrder());
    WordCountReducer reducer = new WordCountReducer();
    PostStep post = new PostStep();

    counter.setProcessor(sorter);
    sorter.setProcessor(reducer);
    reducer.setProcessor(post);

    Document document = new Document();
    document.terms = new ArrayList<String>();
    document.terms.add("one");
    document.terms.add("two");
    document.terms.add("one");
    counter.process(document);

    document.terms = new ArrayList<String>();
    document.terms.add("two");
    document.terms.add("two");
    document.terms.add("three");
    counter.process(document);

    document.terms = new ArrayList<String>();
    document.terms.add("one");
    document.terms.add("three");
    document.terms.add("four");
    counter.process(document);

    counter.close();

    assertEquals(4, post.results.size());

    for (int i = 0; i < post.results.size(); ++i) {
      WordCount wc = post.results.get(i);
      if (Utility.compare(wc.word, Utility.fromString("one")) == 0) {
        assertEquals(3, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("two")) == 0) {
        assertEquals(3, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("three")) == 0) {
        assertEquals(2, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("four")) == 0) {
        assertEquals(1, wc.count);
      }
    }
  }

  public void testCountFilter() throws IOException, IncompatibleProcessorException {
    Parameters p = new Parameters();
    p.set("minThreshold", 2);
    WordCounter counter = new WordCounter(new FakeParameters(p));
    Sorter sorter = new Sorter(new WordCount.WordOrder());
    WordCountReducer reducer = new WordCountReducer();
    WordCountFilter filter = new WordCountFilter(new FakeParameters(p));
    PostStep post = new PostStep();

    counter.setProcessor(sorter);
    sorter.setProcessor(reducer);
    reducer.setProcessor(filter);
    filter.setProcessor(post);

    Document document = new Document();
    document.terms = new ArrayList<String>();
    document.terms.add("one");
    document.terms.add("two");
    document.terms.add("one");
    counter.process(document);

    document.terms = new ArrayList<String>();
    document.terms.add("two");
    document.terms.add("two");
    document.terms.add("three");
    counter.process(document);

    document.terms = new ArrayList<String>();
    document.terms.add("one");
    document.terms.add("three");
    document.terms.add("four");
    counter.process(document);

    counter.close();

    assertEquals(3, post.results.size());

    for (int i = 0; i < post.results.size(); ++i) {
      WordCount wc = post.results.get(i);
      if (Utility.compare(wc.word, Utility.fromString("one")) == 0) {
        assertEquals(3, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("two")) == 0) {
        assertEquals(3, wc.count);
      } else if (Utility.compare(wc.word, Utility.fromString("three")) == 0) {
        assertEquals(2, wc.count);
        //} else if (Utility.compare(wc.word, Utility.fromString("four")) == 0) {
        //  assertEquals(1, wc.count);
      }
    }
  }
}
