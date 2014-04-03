package ciir.proteus.parse;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.types.DocumentSplit;

import java.io.File;
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
    if(!basename.endsWith(".mbtei.gz")) {
      System.err.println("File extension was expected to be .mbtei.gz");
      return basename;
    }
    String[] parts = basename.split(".mbtei.gz");
    return parts[0];
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
}
