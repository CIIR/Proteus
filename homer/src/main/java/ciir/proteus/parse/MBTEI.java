package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jfoley.
 */
public class MBTEI {
  public static String getArchiveIdentifier(DocumentSplit split, Map<String, String> metadata) {
    // MCZ see if we can get the internet archive ID from the metadata
    String archiveID = metadata.get("identifier");

    if (archiveID != null)
      return archiveID;

    // if null - we'll try to figure it out from the file name.

    File f = new File(split.fileName);
    String basename = f.getName();

    String archiveId;
    if(basename.endsWith(".mbtei.gz"))
      archiveId = basename.split(".mbtei.gz")[0];
    else if(basename.endsWith(".toktei.gz"))
      archiveId = basename.split(".toktei.gz")[0];
    else {
      System.err.println("File extension was expected to be .mbtei.gz or .toktei.gz");
      archiveId = basename;
    }
    // put it in the metadata if we had to guess it.
    metadata.put("identifier", archiveId);
    return archiveId;
  }

  public static Document makeDocument(DocumentSplit split, Map<String, String> metadata, String text) {
    Document doc = new Document();
    doc.text = text;
    doc.metadata = metadata;
    doc.name = MBTEI.getArchiveIdentifier(split, metadata);
    return doc;
  }


  public static String scrub(String dirty) {
    if(dirty == null) {
      return "";
    }
    String cleaned = dirty.replaceAll("&apos;", "'");
    cleaned = cleaned.replaceAll("&quot;", "\"");
    cleaned = cleaned.replaceAll("&amp;", "&");
    cleaned = cleaned.replaceAll("[ ]+", " ");
    cleaned = cleaned.replaceAll("(-LRB-|-RRB-)", "");
    return cleaned.trim();
  }

  public static Map<String,String> parseMetadata(XMLStreamReader xml) throws XMLStreamException {
    Map<String,String> metadata = new HashMap<>();

    if(!xml.hasNext())
      return null;

    String currentMetaTag = null;
    StringBuilder tagBuilder = null;

    while(xml.hasNext()) {
      int event = xml.next();

      if (event == XMLStreamConstants.START_ELEMENT) {
        String tag = xml.getLocalName();

        // leave upon hitting a <text> tag; book specific
        if("text".equals(tag)) {
          return metadata;
        }

        if(currentMetaTag == null) {
          // start reading characters
          currentMetaTag = tag;
          tagBuilder = new StringBuilder();
        }
        continue;
      }

      // do nothing until open tag hits
      if(currentMetaTag == null) continue;

      // collect any text or data
      if(event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
        tagBuilder.append(xml.getText()).append(' ');
        continue;
      }

      // add the built text when we hit a close
      if(event == XMLStreamConstants.END_ELEMENT) {
        metadata.put(currentMetaTag, tagBuilder.toString().trim());
        currentMetaTag = null;
        tagBuilder = null;
      }
    }

    return metadata;
  }

}
