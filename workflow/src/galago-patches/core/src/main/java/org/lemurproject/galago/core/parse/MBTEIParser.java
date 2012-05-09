// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.core.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.util.StreamReaderDelegate;
import org.lemurproject.galago.core.types.DocumentSplit;
import java.util.Map;
import java.util.HashMap;

/**
 * Produces page-level postings from books in MBTEI format. Pages with no text
 * are not emitted as documents, and the header is prepended to every emitted Document
 * object. Each document is emitted as an XML file, but only the header retains tags.
 *
 * Otherwise the page text is just in a "<text>" element as one large span of text.
 *
 * @author irmarc
 */
class MBTEIParser implements DocumentStreamParser {

  // External/global switch to flip for different level parsing.
  public static String splitTag;

  // For XML stream processing
  StreamReaderDelegate reader;
  XMLInputFactory factory;
  String headerdata;
  String bookIdentifier;
  int pagenumber;

  // Added by Will
  Map<String, String> metadata;
  int pageCount;
  boolean pageLevel = false;
  String bookContent;

  public MBTEIParser(DocumentSplit split, InputStream is) {
      // Will added: We need a better way to do this
      System.err.println(splitTag);
      if (splitTag.equals("pb"))
	  pageLevel = true;
      else
	  pageLevel = false;

    // XML processing
    try {
      factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_COALESCING, true);
      reader = new StreamReaderDelegate(factory.createXMLStreamReader(is));
      bookIdentifier = getIdentifier(split);
      pagenumber = 0;
      pageCount = 0;
      bookContent = "";
      if (pageLevel)
	  System.err.println("Starting parsing of pages from: " + bookIdentifier);
      else
	  System.err.println("Starting parsing of book: " + bookIdentifier);
      initMetaData();
      System.err.println("Reading header...");
      readHeader();
      System.err.println("Header reading completed...");
      bookContent += headerdata;

    } catch (Exception e) {
      System.err.printf("SKIPPING %s: Caught exception %s\n", split.fileName, e.getMessage());
      reader = null;
    }
  }

  @Override
  public Document nextDocument() throws IOException {
    if (reader == null) return null;

    StringBuilder builder = new StringBuilder();
    int status = 0;
    Document d;
    try {
	if (!reader.hasNext()) {
	    System.err.printf("reader has no more data... [%s, %d]\n", bookIdentifier, pagenumber);
	    return null;
	}

      while (reader.hasNext()) {
          status = reader.next();

        if (status == XMLStreamConstants.START_ELEMENT || status == XMLStreamConstants.END_ELEMENT) {
	    String tag_name = reader.getLocalName();
            if (tag_name.equals("person") || tag_name.equals("location") || tag_name.equals("organization")) {
            	if (status == XMLStreamConstants.START_ELEMENT)
            		builder.append("<").append(reader.getLocalName()).append(" name=\"").append(reader.getAttributeValue("","name")).append("\">");
            	else if(builder.charAt(builder.length()-1) == ' ')
            		builder.insert(builder.length()-1, "</" + reader.getLocalName() + ">");
            	else
            		builder.append("</" + reader.getLocalName() + ">");
          }
        	
        }

        if (status == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("pb")) { //splitTag)) { 
	    //    System.err.println("Building with " + builder.length() + " terms..");
	    d = buildDocument(builder, false);
          pagenumber = Integer.parseInt(reader.getAttributeValue(null, "n"));
          if (d != null) {
            // Have a legitimate document built - send it up.
	      System.err.printf("EMMIT PAGE [%s, %d]: \n", bookIdentifier, pagenumber);
            return d;
          } else if (!pageLevel) {
	      builder = new StringBuilder();
	  }
          // Otherwise, keep going, because we didn't emit a document
        }

        // if it's text, add it to the buffer
        if (status == XMLStreamConstants.CHARACTERS) {
	    String txt = scrub(reader.getText());
	    if (txt.length() > 0) {
		builder.append(txt).append(" ");
	    }
        }
      }

      // All done - either emitting or returning nothing
      d = buildDocument(builder, true);
      System.err.println("Returning Book: " + bookIdentifier);
      return d;
    } catch (Exception e) {
	System.err.printf("EXCEPTION [%s, %d]: %s\n", bookIdentifier, pagenumber, e.getMessage());
	return null;
    }
  }

  public String scrub(String dirty) {
    String cleaned = dirty.replaceAll("&apos;", "'");
    cleaned = cleaned.replaceAll("&quot;", "\"");
    cleaned = cleaned.replaceAll("&amp;", "&");
    cleaned = cleaned.replaceAll("[ ]+", " ");
    cleaned = cleaned.replaceAll("(-LRB-|-RRB-)", "");
    return cleaned.trim();
  }

    private Document buildDocument(StringBuilder builder, boolean EOF) {
	// this could be preventing books with empty last pages from working..

   	String pageIdentifier = String.format("%s_%d", bookIdentifier, pagenumber);

   	if (pageLevel) {
	    if (builder.length() == 0) {
		// we stopped because there are no more tokens
		// if the builder is empty, then we read nothing useful.
		//		System.err.println("Empty Builder Error");
		return null;
	    } 
	    // We got something - let's emit it
	    StringBuilder content = new StringBuilder(headerdata);
	    content.append(builder);
	    content.append("</text></TEI>");
	    
	    Document doc = new Document(pageIdentifier, content.toString());
	    doc.metadata = new HashMap(metadata);
	    doc.metadata.put("pagenumber", String.format("%d", pagenumber));
	    return doc;
	} else {
	    if (builder.length() > 0)
		bookContent += "<page name=\"" + pageIdentifier + "\">" + builder.toString() + "</page>\n";
    	  
	    if (EOF) {
		bookContent += "</text>";
		bookContent += "</TEI>";
		Document doc = new Document(bookIdentifier, bookContent.toString());
		doc.metadata = new HashMap(metadata);
		doc.metadata.put("numpages", String.format("%d", pageCount));
		//		System.out.println("Book Parsed: " + bookIdentifier);
		//		System.out.println(doc.text);
		return doc;
	    }
	    else
		return null;  
	}
  }

  public void readHeader() throws IOException {
    boolean stop = false;
    StringBuilder builder = new StringBuilder();
    int status;
    String previousStart = "";
    try {

      while (!stop && reader.hasNext()) {
        status = reader.next();

        // Emit element starts, text, and element ends
        switch (status) {
          case XMLStreamConstants.CHARACTERS:
            builder.append(reader.getText());
            break;
          case XMLStreamConstants.START_ELEMENT:
            builder.append("<").append(reader.getLocalName()).append(">");
            break;
          case XMLStreamConstants.END_ELEMENT:
            builder.append("</").append(reader.getLocalName()).append(">");
            break;
        }

        if (status == XMLStreamConstants.START_ELEMENT) {
          previousStart = reader.getLocalName();
          // Do we need to stop? DO WE EVEN KNOW HOW TO STOP?
          if (reader.getLocalName().equals("text")) {
            stop = true;
          }
        } else if (status == XMLStreamConstants.CHARACTERS) {
	    addMetaData(previousStart, reader.getText());
        }

      }
    } catch (Exception e) {
      throw new IOException(String.format("While scanning header of split %s", bookIdentifier), e);
    }
    headerdata = builder.toString();
  }

  public String getIdentifier(DocumentSplit split) {
    File f = new File(split.fileName);
    String basename = f.getName();
    String[] parts = basename.split("_");
    return parts[0];
  }

  public void initMetaData() {
      metadata = new HashMap();
  }
  
  public void addMetaData(String key, String value) {
      if(metadata.containsKey(key))
	  metadata.put(key, metadata.get(key) + value.trim());
      else
	  metadata.put(key, value.trim());
  }

}
